package com.jareddlc.turquoisebicuspid;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class BluetoothLeService extends Service {
	// debug data
	private static final String LOG_TAG = "TurquoiseBicuspid:BLE";
	
	// private static objects
	private static Handler mHandler;
	private static String deviceMAC;
	private static UUID mDeviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static Set<BluetoothDevice> pairedDevices;
	private static BluetoothManager mBluetoothManager;
	private static BluetoothAdapter mBluetoothAdapter;
	private static BluetoothDevice mBluetoothDevice;
	private String mBluetoothDeviceAddress;
	private static BluetoothGatt mBluetoothGatt;
	private static EnableBluetoothThread eBluetooth;
	public static CharSequence[] pairedEntries;
	public static CharSequence[] pairedEntryValues;
	public int mConnectionState = 0;
	public static boolean isEnabled = false;
	public static boolean isConnected = false;
	
	private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private static final int REQUEST_ENABLE_BT = 1;
    
    public final static String ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "EXTRA_DATA";
    
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        	Log.d(LOG_TAG, "BluetoothLe onConnectionStateChange: "+status);
            if(newState == BluetoothProfile.STATE_CONNECTED) {
            	Log.d(LOG_TAG, "BluetoothLe Connected to GATT: "+status);
            	mConnectionState = STATE_CONNECTED;
                broadcastUpdate(ACTION_GATT_CONNECTED);
                // Attempts to discover services after successful connection.
                mBluetoothGatt.discoverServices();
            } 
            else if(newState == BluetoothProfile.STATE_DISCONNECTED) {
            	Log.d(LOG_TAG, "BluetoothLe Disconnected from GATT");
            	mConnectionState = STATE_DISCONNECTED;              
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if(status == BluetoothGatt.GATT_SUCCESS) {
            	Log.d(LOG_TAG, "BluetoothLe Service discovered: "+status);
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            }
            else {
            	Log.d(LOG_TAG, "BluetoothLe onServicesDiscovered received: "+status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if(status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };
    
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }
	
	private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
		final Intent intent = new Intent(action);
		// For all other profiles, writes the data formatted in HEX.
        final byte[] data = characteristic.getValue();
        if(data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
	        for(byte byteChar : data) {
	        	stringBuilder.append(String.format("%02X ", byteChar));
	        }   
	        intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
        }
        sendBroadcast(intent);
	}
	
	public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.d(LOG_TAG, "BluetoothLe onBind.");
		return mBinder;
	}
	
	@Override
    public boolean onUnbind(Intent intent) {
		close();
        return super.onUnbind(intent);
    }
	
	private final IBinder mBinder = new LocalBinder();
	
	public boolean initialize() {
		Log.d(LOG_TAG, "BLE Initialize.");
        if(mBluetoothManager == null) {
        	Log.d(LOG_TAG, "Initialize BluetoothManager.");
        	mBluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
            if(mBluetoothManager == null) {
            	Log.e(LOG_TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }        

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if(mBluetoothAdapter == null) {
            Log.e(LOG_TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }
	
    public boolean connect(final String address) {
    	Log.d(LOG_TAG, "BLE connect: "+address);
        if(mBluetoothAdapter == null || address == null) {
	        Log.d(LOG_TAG, "BluetoothAdapter not initialized or unspecified address.");
	        return false;
        }

        if(mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
            Log.d(LOG_TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if(mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            }
            else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.d(LOG_TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(LOG_TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }
    
    public void disconnect() {
    	Log.d(LOG_TAG, "BLE disconnect");
        if(mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.d(LOG_TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }
    
    public void close() {
    	Log.d(LOG_TAG, "BLE close");
        if(mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }
    
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if(mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(LOG_TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }
    
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
		if(mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(LOG_TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
	}
    
    public List<BluetoothGattService> getSupportedGattServices() {
        if(mBluetoothGatt == null) {
        	return null;
        }
        return mBluetoothGatt.getServices();
    }
    
    
    /*****/
    
    public void setHandler(Handler mHndlr) {
    	Log.d(LOG_TAG, "Setting handler");
		mHandler = mHndlr;
    }
	
	public void enableBluetooth() {
		if(!mBluetoothAdapter.isEnabled()) {
			/*Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(enableBtIntent);
			startActivityForResult(enableBtIntent);*/
			//mBluetoothAdapter.enable();
			eBluetooth = new EnableBluetoothThread();
			eBluetooth.start();
		}
	}
	
	
	
	public void disableBluetooth() {
		mBluetoothAdapter.disable();
		isEnabled = false;
	}
	
	public static void setDevice(String devMac) {
		deviceMAC = devMac;
		setPaired();
	}
	
	public static void setPaired() {
		// loop through paired devices
	    for(BluetoothDevice device : pairedDevices) {
	        if(device.getAddress().equals(deviceMAC)) {
	        	Log.d(LOG_TAG, "Set device: "+device.getName()+":"+device.getAddress());
	        	mBluetoothDevice = device;
	        }
	    }
	}
	
	public static void getPaired() {
		if(isEnabled) {			
			pairedDevices = mBluetoothAdapter.getBondedDevices();
			if(pairedDevices.size() > 0) {
				List<CharSequence> entries = new ArrayList<CharSequence>();
				List<CharSequence> values = new ArrayList<CharSequence>();
			    // loop through paired devices
			    for(BluetoothDevice device : pairedDevices) {
			    	Log.d(LOG_TAG, "Device: "+device.getName()+":"+device.getAddress());
			    	entries.add(device.getName());
			    	values.add(device.getAddress());
			    }
			    pairedEntries = entries.toArray(new CharSequence[entries.size()]);
			    pairedEntryValues = values.toArray(new CharSequence[values.size()]);
			}
			else {
				Log.d(LOG_TAG, "No pairedDevices");
			}
		}
		else {
			Log.d(LOG_TAG, "getPaired called without BT enabled");
		}
	}
	
	public CharSequence[] getEntries() {
		if(pairedEntries.length > 0) {
			return pairedEntries;
		}
		else {
			CharSequence[] entries = {"No paired Devices"};
			return entries;
		}
    }

    public CharSequence[] getEntryValues() {
    	if(pairedEntryValues.length > 0) {
			return pairedEntryValues;
		}
		else {
			CharSequence[] entryValues = {"None"};
			return entryValues;
		}
    }
    
    private class EnableBluetoothThread extends Thread {		
		public void run() {
			Log.d(LOG_TAG, "Enabling Bluetooth");
			mBluetoothAdapter.enable();
			while(!mBluetoothAdapter.isEnabled()) {
                try
                {
                    Thread.sleep(100L);
                }
                catch (InterruptedException ie)
                {
                    // unexpected interruption while enabling bluetooth
                    Thread.currentThread().interrupt(); // restore interrupted flag
                    return;
                }
            }
			isEnabled = true;
			Message msg = mHandler.obtainMessage();
            Bundle b = new Bundle();
            b.putString("bluetooth", "isEnabled");
            msg.setData(b);
            mHandler.sendMessage(msg);
			Log.d(LOG_TAG, "Enabled");
		}
	}
}

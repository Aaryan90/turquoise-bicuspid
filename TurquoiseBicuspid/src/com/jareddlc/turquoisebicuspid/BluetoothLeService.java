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
import android.bluetooth.BluetoothProfile;
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
	private static BluetoothAdapter mBluetoothAdapter;
	private static BluetoothDevice mBluetoothDevice;
	private static BluetoothGatt mBluetoothGatt;
	public static CharSequence[] pairedEntries;
	public static CharSequence[] pairedEntryValues;
	private static EnableBluetoothThread enBluetooth;
	public int mConnectionState = 0;
	public static boolean isEnabled = false;
	public static boolean isConnected = false;
	
	private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    
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
		if(mBluetoothGatt != null) {
			mBluetoothGatt.close();
	        mBluetoothGatt = null;
        }
        return super.onUnbind(intent);
    }
	
	private final IBinder mBinder = new LocalBinder();
	
	public static void init(Handler mHndlr) {
		Log.d(LOG_TAG, "Initializing BluetoothLe");
		mHandler = mHndlr;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter == null) {
			Log.d(LOG_TAG, "Bluetooth not supported.");
		}
		if(mBluetoothAdapter.isEnabled()) {
		    //isEnabled = true;
		}
		else {
			Log.d(LOG_TAG, "Bluetooth is not enabled.");
			//isEnabled = false;
		}
	}
	
	public static void enableBluetooth() {   
		if(!mBluetoothAdapter.isEnabled()) {
			//enBluetooth = new EnableBluetoothThread();
			//enBluetooth.start();
		}
	}
	
	public static void disableBluetooth() {
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
	
	public void getPaired() {
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
    
    public static void send(String type, String loop, String time, String repeat, String color) {
		if(isEnabled) {
			String send = type+":"+loop+":"+time+":"+repeat+":"+color+":";
			Log.d(LOG_TAG, "Sending: "+send);
			//TODO: send data
		}
	}
    
    public static void disconnectDevice() {
		if(isConnected) {
			//TODO: disconnect
		}
	}
	
	public void connectDevice() {
		if(isEnabled) {
			//TODO: connect
		}
		else {
			Log.d(LOG_TAG, "BLE Not enabled");
		}
	}
	
	public boolean connectBLE() {
		Log.d(LOG_TAG, "BLE Not enabled");
		
		if(mBluetoothDevice == null) {
			Log.d(LOG_TAG, "Device not found.  Unable to connect.");
	        return false;
        }
		
		mBluetoothGatt = mBluetoothDevice.connectGatt(this, false, mGattCallback);
        Log.d(LOG_TAG, "Trying to create a new connection: "+mBluetoothDevice.getName()+":"+mBluetoothDevice.getAddress());
        mConnectionState = STATE_CONNECTING;
        return true;
	}
	
	public void disconnectBLE() {
        if(mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(LOG_TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }
	
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
	
	private class EnableBluetoothThread extends Thread {
		public void run() {
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
		}
	}
}

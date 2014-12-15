package com.jareddlc.turquoisebicuspid;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
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
import android.preference.ListPreference;
import android.util.Log;

public class BluetoothLeService extends Service {
    // debug data
    private static final String LOG_TAG = "TurquoiseBicuspid:BLE";

    // private static objects
    private static Handler mHandler;
    private static String deviceMAC;
    public final static UUID UUID_HM_RX_TX = UUID.fromString(BluetoothLeGattAttributes.HM_RX_TX);
    private static Set<BluetoothDevice> pairedDevices;
    private static Set<BluetoothDevice> scannedDevices;
    private static BluetoothManager mBluetoothManager;
    private static BluetoothAdapter mBluetoothAdapter;
    private static BluetoothDevice mBluetoothDevice;
    private String mBluetoothDeviceAddress;
    private static BluetoothGatt mBluetoothGatt;
    private static EnableBluetoothThread eBluetooth;
    public static CharSequence[] pairedEntries;
    public static CharSequence[] pairedEntryValues;
    public static CharSequence[] scannedEntries;
    public static CharSequence[] scannedEntryValues;
    
    public static BluetoothGattCharacteristic mWriteCharacteristic;
    public int mConnectionState = 0;
    public static boolean isEnabled = false;
    public static boolean isConnected = false;
    public static boolean isScanning = false;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private static final long SCAN_PERIOD = 5000;
    //private static final int REQUEST_ENABLE_BT = 1;

    public final static String ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "EXTRA_DATA";

    // bluetoothle callback
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(LOG_TAG, "BluetoothLe onConnectionStateChange: "+status);

            if(newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(LOG_TAG, "BluetoothLe Connected to GATT: status:"+status+", state: "+newState);                
                isConnected = true;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(ACTION_GATT_CONNECTED);
                if(mHandler != null) {
                    Message msg = mHandler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("bluetooth", "isConnected");
                    msg.setData(b);
                    mHandler.sendMessage(msg);
                }
                // attempts to discover services after successful connection.
                mBluetoothGatt.discoverServices();
            }
            else if(newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(LOG_TAG, "BluetoothLe Disconnected from GATT");
                isConnected = false;
                mConnectionState = STATE_DISCONNECTED;
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
                if(mHandler != null) {
                    Message msg = mHandler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("bluetooth", "isDisconnected");
                    msg.setData(b);
                    mHandler.sendMessage(msg);
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(LOG_TAG, "onServicesDiscovered");
            if(status == BluetoothGatt.GATT_SUCCESS) {    
                // loops through available GATT Services.
                for(BluetoothGattService gattService : gatt.getServices()) {
                    String uuid = gattService.getUuid().toString();
                    Log.d(LOG_TAG, "onServicesDiscovered: uuid: "+uuid);
                    // look for TurquoiseBicuspid
                    if(BluetoothLeGattAttributes.lookup(uuid, "Unknown service") == "TurquoiseBicuspid") { 
                        Log.d(LOG_TAG, "onServicesDiscovered: Found:TurquoiseBicuspid");
                    }
                    else { 
                        Log.d(LOG_TAG, "onServicesDiscovered: NotFound:TurquoiseBicuspid");
                    }
                    // get characteristic when UUID matches RX/TX UUID
                    mWriteCharacteristic = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
                    Log.d(LOG_TAG, "onServicesDiscovered: getCharacteristic: "+mWriteCharacteristic);
                }
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
                Log.d(LOG_TAG, "BluetoothLe onCharacteristicRead received: "+status);
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(LOG_TAG, "BluetoothLe onCharacteristicChanged received: "+characteristic);
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        Log.d(LOG_TAG, "BluetoothLe broadcastUpdate received: "+characteristic);
        final Intent intent = new Intent(action);
        // for all other profiles, writes the data formatted in HEX.
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
        scannedDevices = new LinkedHashSet<BluetoothDevice>();
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if(mBluetoothAdapter == null) {
            Log.e(LOG_TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        if(mBluetoothAdapter.isEnabled()) {
            isEnabled = true;
        }
        else {
            Log.d(LOG_TAG, "Bluetooth is not enabled.");
            isEnabled = false;
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
        // auto connect to the device
        mBluetoothGatt = device.connectGatt(this, true, mGattCallback);
        Log.d(LOG_TAG, "Trying to create a new connection to: "+address);
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

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if(mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(LOG_TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if(mBluetoothGatt == null) {
            Log.w(LOG_TAG, "getSupportedGattServices mBluetoothGatt not initialized");
            return null;
        }
        Log.d(LOG_TAG, "getSupportedGattServices");
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
            startActivity(enableBtIntent);*/
            //startActivityForResult(enableBtIntent);
            eBluetooth = new EnableBluetoothThread();
            eBluetooth.start();
        }
        else {
            Log.d(LOG_TAG, "enableBluetooth called when BT enabled");
        }
    }

    public void disableBluetooth() {
        mBluetoothAdapter.disable();
        isEnabled = false;
    }

    public static void setDevice(String devMac) {
        deviceMAC = devMac;
        setBluetoothDevice();
    }

    public static void setBluetoothDevice() {
        // loop through paired devices
        if(pairedDevices != null) {
            for(BluetoothDevice device : pairedDevices) {
                if(device.getAddress().equals(deviceMAC)) {
                    Log.d(LOG_TAG, "Set device: "+device.getName()+":"+device.getAddress());
                    mBluetoothDevice = device;
                }
            }
            if(scannedDevices.size() > 0) {
                for(BluetoothDevice device : scannedDevices) {
                    Log.d(LOG_TAG, "Set device: "+device.getName()+":"+device.getAddress());
                    mBluetoothDevice = device;
                }
            }
        }
        else {
            Log.d(LOG_TAG, "setPaired with empty pairedDevices");
        }
        
    }

    public static void setEntries() {
        if(isEnabled) {            
            pairedDevices = mBluetoothAdapter.getBondedDevices();
            if(pairedDevices.size() > 0) {
                List<CharSequence> entries = new ArrayList<CharSequence>();
                List<CharSequence> values = new ArrayList<CharSequence>();
                // loop through paired devices
                for(BluetoothDevice device : pairedDevices) {
                    Log.d(LOG_TAG, "Paired Device: "+device.getName()+":"+device.getAddress());
                    entries.add(device.getName());
                    values.add(device.getAddress());
                }
                // loop trough scanned devices
                if(scannedDevices.size() > 0) {
                    for(BluetoothDevice device : scannedDevices) {
                        // make sure we dont add duplicates
                        if(!entries.contains(device.getName())) {
                            Log.d(LOG_TAG, "Scanned Device: "+device.getName()+":"+device.getAddress());
                            entries.add(device.getName());
                            values.add(device.getAddress());
                        }
                    }
                }
                else {
                    Log.d(LOG_TAG, "No scannedDevices");
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

    public static CharSequence[] getEntries() {
        if(pairedEntries.length > 0) {
            return pairedEntries;
        }
        else {
            CharSequence[] entries = {"No paired Devices"};
            return entries;
        }
    }

    public static CharSequence[] getEntryValues() {
        if(pairedEntryValues.length > 0) {
            return pairedEntryValues;
        }
        else {
            CharSequence[] entryValues = {"None"};
            return entryValues;
        }
    }

    public void send(String type, String loop, String time, String repeat, String color) {
        if(isEnabled) {
            String send = type+":"+loop+":"+time+":"+repeat+":"+color+":";
            Log.d(LOG_TAG, "Sending: "+send);
            BitSmang.toByte(type, loop, time, repeat, color);
            if(isConnected) {
                byte[] sendBytes = send.getBytes();
                mWriteCharacteristic.setValue(sendBytes);
                writeCharacteristic(mWriteCharacteristic);
                setCharacteristicNotification(mWriteCharacteristic, true);
            }
            else {
                Log.d(LOG_TAG, "send called without BT connected");
            }
        }
        else {
            Log.d(LOG_TAG, "send called without BT enabled");
        }
    }

    public void scanLeDevice() {
        Log.d(LOG_TAG, "scanLeDevice");
        if(isEnabled) {
            if(mHandler != null) {
                if(!isScanning) {
                    // Stops scanning after a pre-defined scan period.
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isScanning = false;
                            mBluetoothAdapter.stopLeScan(mScanCallback);
                            Message msg = mHandler.obtainMessage();
                            Bundle b = new Bundle();
                            b.putString("bluetooth", "scanStopped");
                            msg.setData(b);
                            mHandler.sendMessage(msg);
                            setEntries();
                        }
                    }, SCAN_PERIOD);
                    
                    Log.d(LOG_TAG, "scanLeDevice starting scan for: "+SCAN_PERIOD+"ms");
                    isScanning = true;
                    mBluetoothAdapter.startLeScan(mScanCallback);
                }
                else {
                    Log.d(LOG_TAG, "scanLeDevice currently scanning");
                }
            }
            else{
                Log.d(LOG_TAG, "scanLeDevice no mHandler");
            }
        }
        else {
            Log.d(LOG_TAG, "scanLeDevice called without BT enabled");
        }
    }

    private LeScanCallback mScanCallback = new LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if(scannedDevices.add(device)) {
                Log.d(LOG_TAG, device.getName()+" : "+device.getAddress()+" : "+device.getType()+" : "+device.getBondState());
                Message msg = mHandler.obtainMessage();
                Bundle b = new Bundle();
                b.putString("bluetoothDevice", device.getName()+","+device.getAddress());
                //b.putParcelable("bluetoothDevice", device);
                msg.setData(b);
                mHandler.sendMessage(msg);
                setEntries();
            }
        }
    };

    private class EnableBluetoothThread extends Thread {
        public void run() {
            boolean bluetoothEnabled = true;
            long timeStart = Calendar.getInstance().getTimeInMillis();
            Log.d(LOG_TAG, "Enabling Bluetooth: "+timeStart);

            mBluetoothAdapter.enable();
            while(!mBluetoothAdapter.isEnabled()) {
                try
                {
                    long timeDiff =  Calendar.getInstance().getTimeInMillis() - timeStart;
                    if(timeDiff >= 5000) {
                        bluetoothEnabled = false;
                        break;
                    }
                    Thread.sleep(100L);
                }
                catch (InterruptedException ie)
                {
                    // unexpected interruption while enabling bluetooth
                    Thread.currentThread().interrupt(); // restore interrupted flag
                    return;
                }
            }
            if(bluetoothEnabled) {
                isEnabled = true;
                Message msg = mHandler.obtainMessage();
                Bundle b = new Bundle();
                b.putString("bluetooth", "isEnabled");
                msg.setData(b);
                mHandler.sendMessage(msg);
                Log.d(LOG_TAG, "Enabled");
            }
            else {
                Message msg = mHandler.obtainMessage();
                Bundle b = new Bundle();
                b.putString("bluetooth", "isEnabledFailed");
                msg.setData(b);
                mHandler.sendMessage(msg);
                Log.d(LOG_TAG, "Enabling Bluetooth timed out");
            }
            
        }
    }
}

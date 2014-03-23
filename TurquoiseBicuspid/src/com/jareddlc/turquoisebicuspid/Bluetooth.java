package com.jareddlc.turquoisebicuspid;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Bluetooth {
	// debug data
	private static final String LOG_TAG = "TurquoiseBicuspid:Bt";
	
	// private static objects
	private static Handler mHandler;
	private static String deviceMAC;
	private static UUID mDeviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static Set<BluetoothDevice> pairedDevices;
	private static BluetoothAdapter mBluetoothAdapter;
	private static BluetoothDevice mBluetoothDevice;
	private static BluetoothSocket mSocket;
	private static InputStream mInStream;
    private static OutputStream mOutStream;
    private static ConnectThread connect;
    private static HandleThread conx;
    private static EnableBluetoothThread eBluetooth;
	public static CharSequence[] pairedEntries;
	public static CharSequence[] pairedEntryValues;
	public boolean isEnabled = false;
	public boolean isConnected = false;
	
	public Bluetooth(Handler mHndlr) {
		Log.d(LOG_TAG, "Initializing Bluetooth");
		mHandler = mHndlr;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter == null) {
			Log.d(LOG_TAG, "Bluetooth not supported.");
		}
		if(mBluetoothAdapter.isEnabled()) {
		    isEnabled = true;
		}
		else {
			Log.d(LOG_TAG, "Bluetooth is not enabled.");
			isEnabled = false;
		}
	}
	
	public void enableBluetooth() {   
		if(!mBluetoothAdapter.isEnabled()) {
			/*mBluetoothAdapter.enable();
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
			isEnabled = true;*/
			eBluetooth = new EnableBluetoothThread();
			eBluetooth.start();
		}
	}
	
	public void disableBluetooth() {
		mBluetoothAdapter.disable();
		isEnabled = false;
	}
	
	public void setDevice(String devMac) {
		deviceMAC = devMac;
		setPaired();
	}
	
	public void setPaired() {
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
			    	
			        /*if(device.getAddress().equals(deviceMAC)) {
			        	Log.d(LOG_TAG, "Set device: "+device.getName()+":"+device.getAddress());
			        	mBluetoothDevice = device;
			        }*/
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
	
	public void send(String str) {
		if(isEnabled) {
			Log.d(LOG_TAG, "Sending:"+str);
			conx.write(str.getBytes());
		}
	}
	
	public void disconnectDevice() {
		if(isConnected) {
			conx.close();
			connect.close();
		}
	}
	
	public void connectDevice() {
		if(isEnabled) {
			Log.d(LOG_TAG, "Spawning ConnectThread");
			connect = new ConnectThread(mBluetoothDevice);
			connect.start();
		}
		else {
			Log.d(LOG_TAG, "Bt Not enabled");
		}
	}
	
	private class ConnectThread extends Thread {
	    public ConnectThread(BluetoothDevice device) {
	    	Log.d(LOG_TAG, "Initializing ConnectThread");
	        
	        // get a BluetoothSocket to connect with the given BluetoothDevice
	        try {
	        	Log.d(LOG_TAG, "try ConnectThread: "+device.getName()+" with UUID: "+mDeviceUUID.toString());
	        	mSocket = device.createRfcommSocketToServiceRecord(mDeviceUUID);
	        }
	        catch(Exception e) {
	        	Log.e(LOG_TAG, "Error: device.createRfcommSocketToServiceRecord", e);
	        }
	    }
	 
	    public void run() {
	    	Log.d(LOG_TAG, "Running ConnectThread");
	        // Cancel discovery because it will slow down the connection
	        mBluetoothAdapter.cancelDiscovery();
	 
	        try {
	            // connect the device through the socket. This will block until it succeeds or throws an exception
	        	mSocket.connect();
	        	isConnected = true;
	        	Message msg = mHandler.obtainMessage();
	            Bundle b = new Bundle();
	            b.putString("bluetooth", "isConnected");
	            msg.setData(b);
	            mHandler.sendMessage(msg);
	        }
	        catch(IOException connectException) {
	        	Log.e(LOG_TAG, "Error: mmSocket.connect()", connectException);
	            try {
	            	mSocket.close();
	            }
	            catch (IOException closeException) {
	            	Log.e(LOG_TAG, "Error: mmSocket.close()", closeException);
	            }
	            return;
	        }
	 
	        // Do work to manage the connection
	        conx = new HandleThread(mSocket);
	        conx.start();
	        String str = "Android connected.";
	        conx.write(str.getBytes());
	    }
	    	 
	    public void close() {
	        try {
	        	mSocket.close();
	        }
	        catch (IOException e) {
	        	Log.e(LOG_TAG, "Error: mmSocket.close()", e);
	        }
	    }
	}
	
	private class HandleThread extends Thread {
	    public HandleThread(BluetoothSocket socket) {
	    	Log.d(LOG_TAG, "Initializing HandleThread");
	 
	        // get the input and output streams
	        try {
	        	mInStream = socket.getInputStream();
	        	mOutStream = socket.getOutputStream();
	        }
	        catch(IOException e) {
	        	Log.e(LOG_TAG, "Error: socket.getInputStream()/socket.getOutputStream()", e);
	        }
	    }
	 
	    public void run() {
	    	Log.d(LOG_TAG, "Running HandleThread");
	        //byte[] buffer = new byte[1024];
	        //int bytes;
	 
	        // listen to the InputStream
	        /*while(true) {
	            try {
	            	mInStream.read(buffer);
	                //bytes = mInStream.read(buffer);
					// Send the obtained bytes to the UI activity
	                //mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
	            }
	            catch(IOException e) {
	            	Log.e(LOG_TAG, "Error: mInStream.read()", e);
	                break;
	            }
	        }*/
	    }
	 
	    public void write(byte[] bytes) {
	        try {
	            mOutStream.write(bytes);
	        }
	        catch(IOException e) {
	        	Log.e(LOG_TAG, "Error: mOutStream.write()", e);
	        }
	    }
	 
	    public void close() {
	        try {
	        	mInStream.close();
	        	mOutStream.close();
	            mSocket.close();
	            isConnected = false;
	        }
	        catch(IOException e) {
	        	Log.e(LOG_TAG, "Error: mSocket.close()", e);
	        }
	    }
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



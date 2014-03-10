package com.jareddlc.turquoisebicuspid;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class Bt {
	private static final String LOG_TAG = "TurquoiseBicuspid:Bt";
	private static String deviceName = "TurquoiseBicuspid";
	private static String deviceMAC = "20:13:12:06:90:58";
	private static UUID mDeviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	//private static final int REQUEST_ENABLE_BT = 10;
	private static BluetoothAdapter mBluetoothAdapter;
	private static BluetoothDevice mBluetoothDevice;
	private static BluetoothSocket mSocket;
	private static InputStream mInStream;
    private static OutputStream mOutStream;
    private static ConnectedThread conx;
	public static boolean isEnabled;
	
	public Bt() {
		Log.d(LOG_TAG, "Initializing Bluetooth");
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter == null) {
			Log.d(LOG_TAG, "Device does not support Bluetooth.");
		}
		
		if(!mBluetoothAdapter.isEnabled()) {
			Log.d(LOG_TAG, "Device Bluetooth is not enabled.");
			isEnabled = false;
		    //sIntent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		isEnabled = true;
		
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		if(pairedDevices.size() > 0) {
		    // loop through paired devices
		    for(BluetoothDevice device : pairedDevices) {
				// add the name and address to an array adapter to show in a ListView
		        //mArrayAdapter.add(device.getName()+"\n"+device.getAddress());
		        Log.d(LOG_TAG, "Device: "+device.getName()+":"+device.getAddress());
		        if(device.getAddress().equals(deviceMAC)) {
		        	Log.d(LOG_TAG, "Set device: "+device.getName()+":"+device.getAddress());
		        	mBluetoothDevice = device;
		        }
		    }
		}
	}
	
	public void send(String str) {
		Log.d(LOG_TAG, "Sending");
		conx.write(str.getBytes());
	}
	
	public void btConnect() {
		Log.d(LOG_TAG, "Spawning ConnectThread");
		ConnectThread connect = new ConnectThread(mBluetoothDevice);
		connect.start();
	}
	
	public void btConnected() {
		Log.d(LOG_TAG, "Spawning ConnectedThread");
		ConnectedThread connected = new ConnectedThread(mSocket);
		connected.start();
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
	        conx = new ConnectedThread(mSocket);
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
	
	private class ConnectedThread extends Thread {
	    public ConnectedThread(BluetoothSocket socket) {
	    	Log.d(LOG_TAG, "Initializing ConnectedThread");
	 
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
	    	Log.d(LOG_TAG, "Running ConnectedThread");
	        byte[] buffer = new byte[1024];
	        int bytes;
	 
	        // listen to the InputStream
	        while(true) {
	            try {
	                bytes = mInStream.read(buffer);
					// Send the obtained bytes to the UI activity
	                //mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
	            }
	            catch(IOException e) {
	            	Log.e(LOG_TAG, "Error: mInStream.read()", e);
	                break;
	            }
	        }
	    }
	 
	    public void write(byte[] bytes) {
	        try {
	        	Log.d(LOG_TAG, "writting: "+bytes);
	            mOutStream.write(bytes);
	        }
	        catch(IOException e) {
	        	Log.e(LOG_TAG, "Error: mOutStream.write()", e);
	        }
	    }
	 
	    public void close() {
	        try {
	            mSocket.close();
	        }
	        catch(IOException e) {
	        	Log.e(LOG_TAG, "Error: mSocket.close()", e);
	        }
	    }
	}
}



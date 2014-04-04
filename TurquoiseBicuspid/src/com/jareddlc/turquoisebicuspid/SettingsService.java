package com.jareddlc.turquoisebicuspid;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class SettingsService extends Service {
	// debug data
	private static final String LOG_TAG = "TurquoiseBicuspid:SettingsService";
	
	private static boolean smsEnabled = true;
	private static boolean phoneEnabled = true;
	private CallListener callListener;
	private SmsListener smsListener;
	private TelephonyManager telephony;
	private IntentFilter smsFilter;
	
	private Handler mHandler;
	private static Bluetooth bluetooth;
	
	public static void setSms(boolean value) {
		Log.d(LOG_TAG, "SMS: "+value);
		smsEnabled = value;
	}
	
	public static void setPhone(boolean value) {
		Log.d(LOG_TAG, "Phone: "+value);
		phoneEnabled = value;
	}
	
	@SuppressLint("HandlerLeak")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    Toast.makeText(this, "Service running", Toast.LENGTH_SHORT).show();
	    Log.d(LOG_TAG, "Service running");
	    
	    final SavedPreferences sPrefs = new SavedPreferences(this);
	    
	    // register broadcast events
	 	LocalBroadcastManager.getInstance(this).registerReceiver(smsReceiver, new IntentFilter("sms"));

	    // setup bluetooth handler
        mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String bluetoothMsg = msg.getData().getString("bluetooth");
				if(bluetoothMsg.equals("isEnabled")) {
					CharSequence text = "Bluetooth Enabled";
					Toast.makeText(SettingsService.this, text, Toast.LENGTH_SHORT).show();
				}
				if(bluetoothMsg.equals("isConnected")) {
					Log.d(LOG_TAG, "Bluetooth Connected");
					CharSequence text = "Bluetooth Connected";
					Toast.makeText(SettingsService.this, text, Toast.LENGTH_SHORT).show();
				}
				if(bluetoothMsg.equals("isConnectedFailed")) {
					Log.d(LOG_TAG, "Bluetooth Connected Failed");
					CharSequence text = "Bluetooth Connected failed";
					Toast.makeText(SettingsService.this, text, Toast.LENGTH_SHORT).show();
				}
			}
		};
        
        // initialize Bluetooth
        bluetooth = new Bluetooth(mHandler);
        
        if(bluetooth.isEnabled) {
        	bluetooth.getPaired();
			bluetooth.setDevice(sPrefs.saved_pref_connectivity_paired_value);
        	bluetooth.connectDevice();
        }
	    
		// register listeners
	    if(smsEnabled) {
	    	smsListener = new SmsListener();
		    smsFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
		    this.registerReceiver(smsListener, smsFilter);
		    Log.d(LOG_TAG, "sms listening");
	    }
	    
	    if(phoneEnabled) {
	    	callListener = new CallListener();
		    telephony = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		    telephony.listen(callListener, PhoneStateListener.LISTEN_CALL_STATE);
		    Log.d(LOG_TAG, "phone listening");
	    }	    
	    
	    return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show();
		Log.d(LOG_TAG, "Service stopped");
		bluetooth.disconnectDevice();
		
		// unregister listeners
		if(smsEnabled) {
			this.unregisterReceiver(smsListener);
		}
		
		if(phoneEnabled) {
			telephony.listen(callListener, PhoneStateListener.LISTEN_NONE);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(LOG_TAG, "onBind");
		return null;
	}
	
	private BroadcastReceiver smsReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String message = intent.getStringExtra("message");
			String sender = intent.getStringExtra("sender");
			Log.d(LOG_TAG, "Recieved SMS message: "+sender+" - "+message);
		}
	};
}

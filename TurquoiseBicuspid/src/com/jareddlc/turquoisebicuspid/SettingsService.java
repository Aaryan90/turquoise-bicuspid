package com.jareddlc.turquoisebicuspid;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class SettingsService extends Service {
	// debug data
	private static final String LOG_TAG = "TurquoiseBicuspid:SettingsActivity";
	
	private static boolean smsEnabled = true;
	private static boolean phoneEnabled = true;
	private CallListener callListener;
	private SmsListener smsListener;
	private TelephonyManager telephony;
	private IntentFilter smsFilter;
	
	public static void setSms(boolean value) {
		Log.d(LOG_TAG, "SMS: "+value);
		smsEnabled = value;
	}
	
	public static void setPhone(boolean value) {
		Log.d(LOG_TAG, "Phone: "+value);
		phoneEnabled = value;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    Toast.makeText(this, "Service running", Toast.LENGTH_SHORT).show();
	    Log.d(LOG_TAG, "Service running");
	    
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
}

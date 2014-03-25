package com.jareddlc.turquoisebicuspid;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class SettingsService extends Service {
	// debug data
	private static final String LOG_TAG = "TurquoiseBicuspid:SettingsActivity";
	
	private static boolean smsEnabled = false;
	private static boolean phoneEnabled = false;
	private CallListener callListener;
	private SmsListener smsListener;
	
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
	    return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show();
	}


	@Override
	public IBinder onBind(Intent intent) {
		Log.d(LOG_TAG, "onBind");
		return null;
	}
}

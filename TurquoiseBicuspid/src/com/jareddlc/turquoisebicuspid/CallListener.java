package com.jareddlc.turquoisebicuspid;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallListener extends PhoneStateListener {
	private static final String LOG_TAG = "TurquoiseBicuspid:CallListener";
	@Override
	public void onCallStateChanged(int state, String incomingNumber) {
		switch(state) {
			case TelephonyManager.CALL_STATE_IDLE:
			  Log.d(LOG_TAG, "IDLE");
			  break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
			  Log.d(LOG_TAG, "OFFHOOK");
			  break;
			case TelephonyManager.CALL_STATE_RINGING:
			  Log.d(LOG_TAG, "RINGING");
			  break;
		}
	}
}

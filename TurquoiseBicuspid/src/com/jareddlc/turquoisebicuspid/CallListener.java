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
				Log.d(LOG_TAG, "Phone: Idle");
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				Log.d(LOG_TAG, "Phone: Offhook");
				break;
			case TelephonyManager.CALL_STATE_RINGING:
				Log.d(LOG_TAG, "Phone: Ringing - "+incomingNumber);
				break;
		}
	}
}

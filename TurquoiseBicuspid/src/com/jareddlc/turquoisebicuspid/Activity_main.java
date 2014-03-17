package com.jareddlc.turquoisebicuspid;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.ToggleButton;

import com.jareddlc.turquoisebicuspid.Bluetooth;
import com.jareddlc.turquoisebicuspid.CallListener;
import com.jareddlc.turquoisebicuspid.SmsListener;

public class Activity_main extends Activity {
	private static final String LOG_TAG = "TurquoiseBicuspid:Activity_main";
	private Bluetooth bTooth;
	
	// UI
	private static Switch switch_btState;
	private static ToggleButton toggle_on;
	private static ToggleButton toggle_service;
	private static ToggleButton toggle_connect;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_main);
		Log.d(LOG_TAG, "Initializing Activity_main");
		
		// register listeners
		CallListener callListener = new CallListener();
		TelephonyManager telephony = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		telephony.listen(callListener, PhoneStateListener.LISTEN_CALL_STATE);
		
		SmsListener smsListener = new SmsListener();
		IntentFilter smsFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
		this.registerReceiver(smsListener, smsFilter);
		
		// initialize Bluetooth
		bTooth = new Bluetooth();
		bTooth.btConnect();
		switch_btState = (Switch)findViewById(R.id.switch_btState);
		switch_btState.setChecked(bTooth.isEnabled);
		switch_btState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked) {
					Log.d(LOG_TAG, "Switch on");
					bTooth.enableBluetooth();
		        } else {
		        	Log.d(LOG_TAG, "Switch off");
		        	bTooth.disableBluetooth();
		        }
			}
		});
		
		toggle_connect = (ToggleButton)findViewById(R.id.toggle_connect);
		toggle_connect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked) {
					Log.d(LOG_TAG, "Connect");
					bTooth.btConnect();
		        }
				else {
		        	Log.d(LOG_TAG, "Disconnect");
		        	bTooth.btDisconnect();
		        }
			}
		});
		
		toggle_on = (ToggleButton)findViewById(R.id.toggle_on);
		toggle_on.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked) {
					Log.d(LOG_TAG, "TurnOn");
					bTooth.send("TurnOn");
		        }
				else {
		        	bTooth.send("TurnOff");
		        	Log.d(LOG_TAG, "TurnOff");
		        }
			}
		});
		
		toggle_service = (ToggleButton)findViewById(R.id.toggle_service);
		toggle_service.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked) {
					Log.d(LOG_TAG, "Service start");
		        }
				else {
		        	Log.d(LOG_TAG, "Service stop");
		        }
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}

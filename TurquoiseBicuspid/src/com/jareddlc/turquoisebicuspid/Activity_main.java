package com.jareddlc.turquoisebicuspid;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.ToggleButton;

import com.jareddlc.turquoisebicuspid.Bt;
import com.jareddlc.turquoisebicuspid.R.id;

public class Activity_main extends Activity {
	private static final String LOG_TAG = "TurquoiseBicuspid:Activity_main";
	private static final int REQUEST_ENABLE_BT = 10;
	private Bt bTooth;
	
	// UI
	private static Switch switch_btState;
	private static Button button_send;
	private static ToggleButton toggle_on;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_main);
		Log.d(LOG_TAG, "Initializing Activity_main");
		
		// initialize Bluetooth
		bTooth = new Bt();
		bTooth.btConnect();
		switch_btState = (Switch)findViewById(R.id.switch_btState);
		switch_btState.setChecked(bTooth.isEnabled);
		
		button_send = (Button)findViewById(R.id.button_send);
		button_send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	bTooth.send("Send Data");
                Log.d(LOG_TAG, "Send Data");
            }
        });
		
		toggle_on = (ToggleButton)findViewById(R.id.toggle_on);
		toggle_on.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked) {
					bTooth.send("TurnOn");
		        } else {
		        	bTooth.send("TurnOff");
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

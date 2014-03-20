package com.jareddlc.turquoisebicuspid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

public class SettingsActivity extends Activity {
	// debug data
	private static final String LOG_TAG = "TurquoiseBicuspid:SettingsActivity";
	public static final String PREFS_NAME = "TurquoiseBicuspidSettings";
	
	private static SharedPreferences preferences;
	private static SharedPreferences.Editor editor;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // load the PreferenceFragment
        Log.d(LOG_TAG, "Loading PreferenceFragment");
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
        
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }
 
    /**
     * Simple preferences without {@code PreferenceActivity} and headers.
     */
    public static class SettingsFragment extends PreferenceFragment {
    	// app data
    	private static final String LOG_TAG = "TurquoiseBicuspid:SettingsFragment";
    	
    	// UI objects
    	private static SwitchPreference pref_connectivity_bluetooth;
    	private static CheckBoxPreference pref_connectivity_connected;
    	private static CheckBoxPreference pref_service;
    	private static ListPreference pref_connectivity_paired;
    	private static Preference pref_device;
    	
    	// private static objects
    	private static Handler mHandler;
    	private static Bluetooth bluetooth;

		@SuppressLint("HandlerLeak")
		public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            
            // load from preferences from xml
            addPreferencesFromResource(R.xml.preferences);
            
            // setup handler
            mHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					Log.d(LOG_TAG, "Message received:");
					setPairedDevices();
					pref_connectivity_bluetooth.setChecked(true);
				}
			};
            
            // initialize Bluetooth
            bluetooth = new Bluetooth(mHandler);
            
            // load paired devices
            pref_connectivity_paired = (ListPreference) getPreferenceManager().findPreference("pref_connectivity_paired");
            if(bluetooth.isEnabled) {
            	/*bluetooth.getPaired();
            	pref_connectivity_paired.setEntries(bluetooth.getEntries());
                pref_connectivity_paired.setEntryValues(bluetooth.getEntryValues());*/
            	setPairedDevices();
            }
            
            // UI listeners
            pref_connectivity_paired.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {		
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Log.d(LOG_TAG, preference.getKey()+" clicked");
					return true;
				}
			});
            pref_connectivity_paired.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Log.d(LOG_TAG, preference.getKey()+": "+newValue.toString());
					bluetooth.setDevice(newValue.toString());
					return true;
				}
			});
            
            pref_connectivity_bluetooth = (SwitchPreference) getPreferenceManager().findPreference("pref_connectivity_bluetooth");
            pref_connectivity_bluetooth.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Log.d(LOG_TAG, preference.getKey()+" changed to: "+newValue.toString());
					boolean value = (Boolean)newValue;
					if(value) {
						bluetooth.enableBluetooth();
					}
					else {
						bluetooth.disableBluetooth();
					}
					return true;
				}
			});
            
            // grab current Bluetooth state
            if(bluetooth.isEnabled) {
            	pref_connectivity_bluetooth.setChecked(true);
            }
            else {
            	pref_connectivity_bluetooth.setChecked(false);
            }
            
            pref_connectivity_connected = (CheckBoxPreference) getPreferenceManager().findPreference("pref_connectivity_connected");
            pref_connectivity_connected.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Log.d(LOG_TAG, preference.getKey()+" changed to: "+newValue.toString());
					boolean value = (Boolean)newValue;
					if(value) {
						bluetooth.connectDevice();
					}
					else {
						bluetooth.disconnectDevice();
					}
					return true;
				}
			});
            
            pref_service = (CheckBoxPreference) getPreferenceManager().findPreference("pref_service");
            pref_service.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Log.d(LOG_TAG, preference.getKey()+" changed to: "+newValue.toString());
					boolean value = (Boolean)newValue;
					if(value) {
						Log.d(LOG_TAG, "Should turn on service");
					}
					else {
						Log.d(LOG_TAG, "Should turn off service");
					}
					return true;
				}
			});

            pref_device = (Preference) getPreferenceManager().findPreference("pref_device");
            pref_device.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {		
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Log.d(LOG_TAG, preference.getKey()+" clicked");
					bluetooth.send("TurnOn");
					return true;
				}
			});
        }
		
		public void setPairedDevices() {
			bluetooth.getPaired();
        	pref_connectivity_paired.setEntries(bluetooth.getEntries());
            pref_connectivity_paired.setEntryValues(bluetooth.getEntryValues());
        }
    }
}

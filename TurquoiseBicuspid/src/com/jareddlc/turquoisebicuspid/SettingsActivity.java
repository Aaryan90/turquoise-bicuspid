package com.jareddlc.turquoisebicuspid;

import android.app.Activity;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.SwitchPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

public class SettingsActivity extends Activity {
	private static final String LOG_TAG = "TurquoiseBicuspid:SettingsActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // load the PreferenceFragment
        Log.d(LOG_TAG, "Loading PreferenceFragment");
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }
 
    /**
     * Simple preferences without {@code PreferenceActivity} and headers.
     */
    public static class SettingsFragment extends PreferenceFragment {
    	// debug data
    	private static final String LOG_TAG = "TurquoiseBicuspid:SettingsFragment";
    	
    	// UI objects
    	private static SwitchPreference pref_connectivity_bluetooth;
    	private static CheckBoxPreference pref_connectivity_connected;
    	private static CheckBoxPreference pref_service;
    	private static ListPreference pref_connectivity_paired;
    	private static Preference pref_device;
    	
    	// private static objects
    	private static Bluetooth bluetooth;
    	
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            
            // initialize Bluetooth
            bluetooth = new Bluetooth();
            
            // load paired devices
            pref_connectivity_paired = (ListPreference) getPreferenceManager().findPreference("pref_connectivity_paired");
            if(bluetooth.isEnabled) {
            	pref_connectivity_paired.setEntries(bluetooth.getEntries());
                pref_connectivity_paired.setEntryValues(bluetooth.getEntryValues());
            }
            
            pref_connectivity_paired.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {		
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Log.d(LOG_TAG, preference.getKey()+" clicked");
					bluetooth.getPaired();
					if(bluetooth.isEnabled) {
		            	pref_connectivity_paired.setEntries(bluetooth.getEntries());
		                pref_connectivity_paired.setEntryValues(bluetooth.getEntryValues());
		            }
					return true;
				}
			});
            
            // UI listeners
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
            
            pref_connectivity_connected = (CheckBoxPreference) getPreferenceManager().findPreference("pref_connectivity_connected");
            pref_connectivity_connected.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Log.d(LOG_TAG, preference.getKey()+" changed to: "+newValue.toString());
					return true;
				}
			});
            
            pref_service = (CheckBoxPreference) getPreferenceManager().findPreference("pref_service");
            pref_service.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Log.d(LOG_TAG, preference.getKey()+" changed to: "+newValue.toString());
					return true;
				}
			});

            pref_device = (Preference) getPreferenceManager().findPreference("pref_device");
            pref_device.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {		
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Log.d(LOG_TAG, preference.getKey()+" clicked");
					return true;
				}
			});
        }
    }
}

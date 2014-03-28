package com.jareddlc.turquoisebicuspid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

public class SettingsActivity extends Activity {
	// debug data
	private static final String LOG_TAG = "TurquoiseBicuspid:SettingsActivity";
	public static final String PREFS_NAME = "TurquoiseBicuspidSettings";
	
	private static SharedPreferences preferences;
	private static SharedPreferences.Editor editor;
	private static SavedPreferences sPrefs;
	private static Context context;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // load the PreferenceFragment
        Log.d(LOG_TAG, "Loading PreferenceFragment");
        context = getApplicationContext();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
        sPrefs = new SavedPreferences(context);
        
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
    	private static Preference pref_sms;
    	private static Preference pref_phone;
    	
    	// private static objects
    	private static Handler mHandler;
    	private static Bluetooth bluetooth;

		@SuppressLint("HandlerLeak")
		public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            
            // load from preferences from xml
            addPreferencesFromResource(R.xml.preferences);
            getActivity();
            
            // setup bluetooth handler
            mHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					String bluetoothMsg = msg.getData().getString("bluetooth");
					if(bluetoothMsg.equals("isEnabled")) {
						CharSequence text = "Bluetooth Enabled";
						Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
						
						SettingsFragment.this.restorePreferences();				
						pref_connectivity_bluetooth.setChecked(true);
						pref_connectivity_paired.setEnabled(true);
					}
					if(bluetoothMsg.equals("isConnected")) {
						Log.d(LOG_TAG, "Bluetooth Connected");
						CharSequence text = "Bluetooth Connected";
						Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
						pref_connectivity_connected.setEnabled(true);
					}
					if(bluetoothMsg.equals("isConnectedFailed")) {
						Log.d(LOG_TAG, "Bluetooth Connected Failed");
						CharSequence text = "Bluetooth Connected failed";
						Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
						pref_connectivity_connected.setEnabled(true);
						pref_connectivity_connected.setChecked(false);
					}
				}
			};
            
            // initialize Bluetooth
            bluetooth = new Bluetooth(mHandler);
            
            // UI listeners
            pref_connectivity_paired = (ListPreference) getPreferenceManager().findPreference("pref_connectivity_paired");
            pref_connectivity_paired.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {		
				@Override
				public boolean onPreferenceClick(Preference preference) {
					return true;
				}
			});
            pref_connectivity_paired.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					bluetooth.setDevice(newValue.toString());
					int index = pref_connectivity_paired.findIndexOfValue(newValue.toString());
				    CharSequence[] entries = pref_connectivity_paired.getEntries();
				    editor.putString("saved_pref_connectivity_paired_value", newValue.toString());
				    editor.putString("saved_pref_connectivity_paired_entry", entries[index].toString());
					editor.commit();
					return true;
				}
			});
            
            pref_connectivity_bluetooth = (SwitchPreference) getPreferenceManager().findPreference("pref_connectivity_bluetooth");
            pref_connectivity_bluetooth.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					if((Boolean)newValue) {
						bluetooth.enableBluetooth();
						CharSequence text = "Enabling...";
						Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
						pref_connectivity_paired.setEnabled(false);
						editor.putBoolean("saved_pref_connectivity_bluetooth", true);
						editor.commit();
					}
					else {
						bluetooth.disableBluetooth();
						editor.putBoolean("saved_pref_connectivity_bluetooth", false);
						editor.commit();
					}
					return true;
				}
			});
            
            pref_connectivity_connected = (CheckBoxPreference) getPreferenceManager().findPreference("pref_connectivity_connected");
            pref_connectivity_connected.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					if((Boolean)newValue) {
						bluetooth.connectDevice();
						CharSequence text = "Connecting...";
						Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
						pref_connectivity_connected.setEnabled(false);
						editor.putBoolean("saved_pref_connectivity_connected", true);
						editor.commit();
					}
					else {
						bluetooth.disconnectDevice();
						editor.putBoolean("saved_pref_connectivity_connected", false);
						editor.commit();
					}
					return true;
				}
			});
            
            pref_service = (CheckBoxPreference) getPreferenceManager().findPreference("pref_service");
            pref_service.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					if((Boolean)newValue) {
						getActivity().startService(new Intent(getActivity(),SettingsService.class));
						editor.putBoolean("saved_pref_service", true);
						editor.commit();
					}
					else {
						getActivity().stopService(new Intent(getActivity(),SettingsService.class));
						editor.putBoolean("saved_pref_service", false);
						editor.commit();
					}
					return true;
				}
			});

            pref_device = (Preference) getPreferenceManager().findPreference("pref_device");
            pref_device.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {		
				@Override
				public boolean onPreferenceClick(Preference preference) {
					bluetooth.send("TurnOn");
					return true;
				}
			});
            
            pref_sms = (CheckBoxPreference) getPreferenceManager().findPreference("pref_sms");
            pref_sms.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					if((Boolean)newValue) {
						SettingsService.setSms(true);
						editor.putBoolean("saved_pref_sms", true);
						editor.commit();
					}
					else {
						SettingsService.setSms(false);
						editor.putBoolean("saved_pref_sms", false);
						editor.commit();
					}
					return true;
				}
			});
            
            pref_phone = (CheckBoxPreference) getPreferenceManager().findPreference("pref_phone");
            pref_phone.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					if((Boolean)newValue) {
						SettingsService.setPhone(true);
						editor.putBoolean("saved_pref_phone", true);
						editor.commit();
					}
					else {
						SettingsService.setPhone(false);
						editor.putBoolean("saved_pref_phone", false);
						editor.commit();
					}
					return true;
				}
			});
            
            // restore preferences
            if(bluetooth.isEnabled) {
            	pref_connectivity_bluetooth.setChecked(true);
            	this.restorePreferences();
            	if(!bluetooth.isConnected) {
            		pref_connectivity_connected.setChecked(false);
            		pref_service.setChecked(false);
            	}
            }
            else {
            	pref_connectivity_bluetooth.setChecked(false);
            }
        }
		
		public void restorePreferences() {
			Log.d(LOG_TAG, "Restore paired device: "+sPrefs.saved_pref_connectivity_paired_value+":"+sPrefs.saved_pref_connectivity_paired_entry);
			bluetooth.getPaired();
			bluetooth.setDevice(sPrefs.saved_pref_connectivity_paired_value);
			pref_connectivity_paired.setSummary(sPrefs.saved_pref_connectivity_paired_entry);
			pref_connectivity_paired.setEntries(bluetooth.getEntries());
            pref_connectivity_paired.setEntryValues(bluetooth.getEntryValues());
		}
     }
}

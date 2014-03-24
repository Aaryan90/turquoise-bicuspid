package com.jareddlc.turquoisebicuspid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceCategory;
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
	private static Context context;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // load the PreferenceFragment
        Log.d(LOG_TAG, "Loading PreferenceFragment");
        context = getApplicationContext();
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
            
            // setup handler
            mHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					String bluetoothMsg = msg.getData().getString("bluetooth");
					Log.d(LOG_TAG, "Message received:"+bluetoothMsg);
					if(bluetoothMsg.equals("isEnabled")) {
						
						setPairedDevices();
						pref_connectivity_bluetooth.setChecked(true);
						CharSequence text = "Bluetooth Enabled";
						Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
						pref_connectivity_paired.setEnabled(true);
						
						// restore prefs
						Log.d(LOG_TAG, "Restoring preferences");
						restorePreferences();
						setPairedDevices();
					}
					if(bluetoothMsg.equals("isConnected")) {
						Log.d(LOG_TAG, "Bluetooth Connected");
						CharSequence text = "Bluetooth Connected";
						Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
						pref_connectivity_connected.setEnabled(true);
					}
					if(bluetoothMsg.equals("isConnectedFailed")) {
						Log.d(LOG_TAG, "Bluetooth Connected");
						CharSequence text = "Bluetooth Connected failed";
						Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
						pref_connectivity_connected.setEnabled(true);
						pref_connectivity_connected.setChecked(false);
					}
				}
			};
            
            // initialize Bluetooth
            bluetooth = new Bluetooth(mHandler);
            
            // load paired devices
            pref_connectivity_paired = (ListPreference) getPreferenceManager().findPreference("pref_connectivity_paired");
            if(bluetooth.isEnabled) {
            	setPairedDevices();
            	restorePreferences();
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
					editor.putString("saved_pref_connectivity_paired_value", newValue.toString());
					editor.commit();
					int index = pref_connectivity_paired.findIndexOfValue(newValue.toString());
				    CharSequence[] entries = pref_connectivity_paired.getEntries();
				    editor.putString("saved_pref_connectivity_paired_entry", entries[index].toString());
					editor.commit();
				    Log.d(LOG_TAG, preference.getKey()+": "+entries[index]);
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
						CharSequence text = "Enabling...";
						Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
						pref_connectivity_paired.setEnabled(false);
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
						CharSequence text = "Connecting...";
						Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
						pref_connectivity_connected.setEnabled(false);
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
            
            pref_sms = (CheckBoxPreference) getPreferenceManager().findPreference("pref_sms");
            pref_sms.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Log.d(LOG_TAG, preference.getKey()+" changed to: "+newValue.toString());
					boolean value = (Boolean)newValue;
					if(value) {
						Log.d(LOG_TAG, "Should turn on sms notifications");
					}
					else {
						Log.d(LOG_TAG, "Should turn off sms notifications");
					}
					return true;
				}
			});
            
            pref_phone = (CheckBoxPreference) getPreferenceManager().findPreference("pref_phone");
            pref_phone.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Log.d(LOG_TAG, preference.getKey()+" changed to: "+newValue.toString());
					boolean value = (Boolean)newValue;
					if(value) {
						Log.d(LOG_TAG, "Should turn on phone notifications");
					}
					else {
						Log.d(LOG_TAG, "Should turn off phone notifications");
					}
					return true;
				}
			});
            
            
        }
		
		public void setPairedDevices() {
			bluetooth.getPaired();
        	pref_connectivity_paired.setEntries(bluetooth.getEntries());
            pref_connectivity_paired.setEntryValues(bluetooth.getEntryValues());
        }
		public void restorePreferences() {
			String saved_pref_connectivity_paired_value = preferences.getString("saved_pref_connectivity_paired_value", "DEFAULT");
			String saved_pref_connectivity_paired_entry = preferences.getString("saved_pref_connectivity_paired_entry", "DEFAULT");
			Log.d(LOG_TAG, "Restore preference: "+saved_pref_connectivity_paired_entry+"=:"+saved_pref_connectivity_paired_value);
			bluetooth.setDevice(saved_pref_connectivity_paired_value);
			pref_connectivity_paired.setSummary(saved_pref_connectivity_paired_entry);
		}
     }
}

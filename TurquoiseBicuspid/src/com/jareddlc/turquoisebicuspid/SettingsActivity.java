package com.jareddlc.turquoisebicuspid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
    	// app data
    	private static final String LOG_TAG = "TurquoiseBicuspid:SettingsFragment";
    	
    	// UI objects
    	private static SwitchPreference pref_connectivity_bluetooth;
    	private static CheckBoxPreference pref_service;
    	private static ListPreference pref_connectivity_paired;
    	private static Preference pref_clear;
    	private static Preference pref_sms;
    	private static ListPreference pref_sms_type;
    	private static ListPreference pref_sms_time;
    	private static ListPreference pref_sms_loop;
    	private static ColorPickerPreference pref_sms_color;
    	private static Preference pref_phone;
    	private static ListPreference pref_phone_type;
    	private static ListPreference pref_phone_time;
    	private static ListPreference pref_phone_loop;
    	private static ColorPickerPreference pref_phone_color;
    	private static ListPreference pref_repeat;
    	
    	// private static objects
    	private static Handler mHandler;
    	private static Bluetooth bluetooth;

		@SuppressLint("HandlerLeak")
		public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            
            // load preferences from xml
            addPreferencesFromResource(R.xml.preferences);
            
            // load saved preferences
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            final Editor editor = preferences.edit();
            final SavedPreferences sPrefs = new SavedPreferences(getActivity());
            
            // setup bluetooth handler
            mHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					String bluetoothMsg = msg.getData().getString("bluetooth");
					if(bluetoothMsg.equals("isEnabled")) {
						CharSequence text = "Bluetooth Enabled";
						Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
						
						SettingsFragment.this.restorePreferences(sPrefs);				
						pref_connectivity_bluetooth.setChecked(true);
						pref_connectivity_paired.setEnabled(true);
					}
					if(bluetoothMsg.equals("isConnected")) {
						Log.d(LOG_TAG, "Bluetooth Connected");
						CharSequence text = "Bluetooth Connected";
						Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
						pref_service.setEnabled(true);
					}
					if(bluetoothMsg.equals("isConnectedFailed")) {
						Log.d(LOG_TAG, "Bluetooth Connected Failed");
						CharSequence text = "Bluetooth Connected failed";
						Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
						pref_service.setEnabled(true);
						pref_service.setChecked(false);
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
						Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
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
						bluetooth.disconnectDevice();
						editor.putBoolean("saved_pref_service", false);
						editor.commit();
					}
					return true;
				}
			});
            
            pref_clear = (Preference) getPreferenceManager().findPreference("pref_clear");
            pref_clear.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {		
				@Override
				public boolean onPreferenceClick(Preference preference) {
					bluetooth.send("blink", "3", "50", "-1", "ffffff");
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
            pref_sms_type = (ListPreference) getPreferenceManager().findPreference("pref_sms_type");
            pref_sms_type.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					int index = pref_sms_type.findIndexOfValue(newValue.toString());
				    CharSequence[] entries = pref_sms_type.getEntries();
				    editor.putString("saved_pref_sms_type_value", newValue.toString());
				    editor.putString("saved_pref_sms_type_entry", entries[index].toString());
					editor.commit();
					bluetooth.send(newValue.toString(), pref_sms_loop.getValue(), pref_sms_time.getValue(), "0", pref_sms_color.getHexValue());
					return true;
				}
			});
            pref_sms_time = (ListPreference) getPreferenceManager().findPreference("pref_sms_time");
            pref_sms_time.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					int index = pref_sms_time.findIndexOfValue(newValue.toString());
				    CharSequence[] entries = pref_sms_time.getEntries();
				    editor.putString("saved_pref_sms_time_value", newValue.toString());
				    editor.putString("saved_pref_sms_time_entry", entries[index].toString());
					editor.commit();
					bluetooth.send(pref_sms_type.getValue(), pref_sms_loop.getValue(), newValue.toString(), "0", pref_sms_color.getHexValue());
					return true;
				}
			});
            pref_sms_loop = (ListPreference) getPreferenceManager().findPreference("pref_sms_loop");
            pref_sms_loop.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					int index = pref_sms_loop.findIndexOfValue(newValue.toString());
				    CharSequence[] entries = pref_sms_loop.getEntries();
				    editor.putString("saved_pref_sms_loop_value", newValue.toString());
				    editor.putString("saved_pref_sms_loop_entry", entries[index].toString());
					editor.commit();
					bluetooth.send(pref_sms_type.getValue(), newValue.toString(), pref_sms_time.getValue(), "0", pref_sms_color.getHexValue());
					return true;
				}
			});
            
            pref_sms_color = (ColorPickerPreference) getPreferenceManager().findPreference("pref_sms_color");
            pref_sms_color.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					editor.putString("saved_pref_sms_color", Integer.toHexString((Integer)newValue));
					editor.commit();
					bluetooth.send(pref_sms_type.getValue(), pref_sms_loop.getValue(), pref_sms_time.getValue(), "0", pref_sms_color.getHexValue());
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
            pref_phone_type = (ListPreference) getPreferenceManager().findPreference("pref_phone_type");
            pref_phone_type.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					int index = pref_phone_type.findIndexOfValue(newValue.toString());
				    CharSequence[] entries = pref_phone_type.getEntries();
				    editor.putString("saved_pref_phone_type_value", newValue.toString());
				    editor.putString("saved_pref_phone_type_entry", entries[index].toString());
					editor.commit();
					bluetooth.send(newValue.toString(), pref_phone_loop.getValue(), pref_phone_time.getValue(), "0", pref_phone_color.getHexValue());
					return true;
				}
			});
            pref_phone_time = (ListPreference) getPreferenceManager().findPreference("pref_phone_time");
            pref_phone_time.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					int index = pref_phone_time.findIndexOfValue(newValue.toString());
				    CharSequence[] entries = pref_phone_time.getEntries();
				    editor.putString("saved_pref_phone_time_value", newValue.toString());
				    editor.putString("saved_pref_phone_time_entry", entries[index].toString());
					editor.commit();
					bluetooth.send(pref_phone_type.getValue(), pref_phone_loop.getValue(), newValue.toString(), "0", pref_phone_color.getHexValue());
					return true;
				}
			});
            pref_phone_loop = (ListPreference) getPreferenceManager().findPreference("pref_phone_loop");
            pref_phone_loop.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					int index = pref_phone_loop.findIndexOfValue(newValue.toString());
				    CharSequence[] entries = pref_phone_loop.getEntries();
				    editor.putString("saved_pref_phone_loop_value", newValue.toString());
				    editor.putString("saved_pref_phone_loop_entry", entries[index].toString());
					editor.commit();
					bluetooth.send(pref_phone_type.getValue(), newValue.toString(), pref_phone_time.getValue(), "0", pref_phone_color.getHexValue());
					return true;
				}
			});
            
            pref_phone_color = (ColorPickerPreference) getPreferenceManager().findPreference("pref_phone_color");
            pref_phone_color.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					editor.putString("saved_pref_phone_color", Integer.toHexString((Integer)newValue));
					editor.commit();
					bluetooth.send(pref_phone_type.getValue(), pref_phone_loop.getValue(), pref_phone_time.getValue(), "0", pref_phone_color.getHexValue());
					return true;
				}
			});
            
            pref_repeat = (ListPreference) getPreferenceManager().findPreference("pref_repeat");
            pref_repeat.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					int index = pref_repeat.findIndexOfValue(newValue.toString());
					CharSequence[] entries = pref_repeat.getEntries();
					editor.putString("saved_pref_repeat_value", newValue.toString());
					editor.putString("saved_pref_repeat_entry", entries[index].toString());
					editor.commit();
					return true;
				}
			});
            
            // restore preferences
            if(bluetooth.isEnabled) {
            	pref_connectivity_bluetooth.setChecked(true);
            	editor.putBoolean("saved_pref_connectivity_bluetooth", true);
				editor.commit();
            	this.restorePreferences(sPrefs);
            	if(!bluetooth.isConnected) {
            		pref_service.setChecked(false);
            	}
            }
            else {
            	pref_connectivity_bluetooth.setChecked(false);
            	editor.putBoolean("saved_pref_connectivity_bluetooth", false);
				editor.commit();
            }
        }
		
		public void restorePreferences(SavedPreferences sPrefs) {
			Log.d(LOG_TAG, "Restore paired device: "+sPrefs.saved_pref_connectivity_paired_value+":"+sPrefs.saved_pref_connectivity_paired_entry);
			bluetooth.getPaired();
			bluetooth.setDevice(sPrefs.saved_pref_connectivity_paired_value);
			pref_connectivity_paired.setSummary(sPrefs.saved_pref_connectivity_paired_entry);
			pref_connectivity_paired.setEntries(bluetooth.getEntries());
            pref_connectivity_paired.setEntryValues(bluetooth.getEntryValues());
		}
     }
}

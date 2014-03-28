package com.jareddlc.turquoisebicuspid;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class SavedPreferences {
	private static final String LOG_TAG = "TurquoiseBicuspid:SettingsActivity";
	public static final String PREFS_NAME = "TurquoiseBicuspidSettings";
	public static final String PREFS_DEFAULT = "DEFAULT";
	
	// saved values
	public boolean saved_pref_connectivity_bluetooth;
	public boolean saved_pref_connectivity_paired;
	public boolean saved_pref_connectivity_connected;
	public boolean saved_pref_device;
	public boolean saved_pref_service;
	public boolean saved_pref_sms;
	public boolean saved_pref_phone;
	public String saved_pref_connectivity_paired_value;
	public String saved_pref_connectivity_paired_entry;
	
	private static SharedPreferences preferences;
	private static SharedPreferences.Editor editor;
	
	public SavedPreferences(Context context) {
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = preferences.edit();
		loadSaved();
	}
	
	public void loadSaved() {
		Log.d(LOG_TAG, "Loading saved preferences");
		
		saved_pref_connectivity_bluetooth = preferences.getBoolean("saved_pref_connectivity_bluetooth", false);
		saved_pref_connectivity_paired = preferences.getBoolean("saved_pref_connectivity_paired", false);
		saved_pref_connectivity_connected = preferences.getBoolean("saved_pref_connectivity_connected", false);
		saved_pref_device = preferences.getBoolean("saved_pref_device", false);
		saved_pref_service = preferences.getBoolean("saved_pref_service", false);
		saved_pref_sms = preferences.getBoolean("saved_pref_sms", false);
		saved_pref_phone = preferences.getBoolean("saved_pref_phone", false);
		saved_pref_connectivity_paired_value = preferences.getString("saved_pref_connectivity_paired_value", PREFS_DEFAULT);
		saved_pref_connectivity_paired_entry = preferences.getString("saved_pref_connectivity_paired_entry", PREFS_DEFAULT);

	}
}

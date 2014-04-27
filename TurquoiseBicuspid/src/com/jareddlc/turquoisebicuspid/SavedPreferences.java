package com.jareddlc.turquoisebicuspid;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class SavedPreferences {
	private static final String LOG_TAG = "TurquoiseBicuspid:SavedPreferences";
	public static final String PREFS_NAME = "TurquoiseBicuspidSettings";
	public static final String PREFS_DEFAULT = "DEFAULT";
	
	// saved values
	public boolean saved_pref_connectivity_bluetooth;
	public boolean saved_pref_connectivity_paired;
	public boolean saved_pref_service;
	public boolean saved_pref_sms;
	public String saved_pref_sms_type_value;
	public String saved_pref_sms_type_entry;
	public String saved_pref_sms_time_value;
	public String saved_pref_sms_time_entry;
	public String saved_pref_sms_loop_value;
	public String saved_pref_sms_loop_entry;
	public String saved_pref_sms_repeat_value;
	public String saved_pref_sms_repeat_entry;
	public String saved_pref_sms_color;
	public boolean saved_pref_phone;
	public String saved_pref_phone_type_value;
	public String saved_pref_phone_type_entry;
	public String saved_pref_phone_time_value;
	public String saved_pref_phone_time_entry;
	public String saved_pref_phone_loop_value;
	public String saved_pref_phone_loop_entry;
	public String saved_pref_phone_repeat_value;
	public String saved_pref_phone_repeat_entry;
	public String saved_pref_phone_color;
	public String saved_pref_connectivity_paired_value;
	public String saved_pref_connectivity_paired_entry;
	public String saved_pref_repeat_value;
	public String saved_pref_repeat_entry;
	
	public SavedPreferences(Context context) {
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        //Editor editor = preferences.edit();

        Log.d(LOG_TAG, "Loading saved preferences");
		saved_pref_connectivity_bluetooth = preferences.getBoolean("saved_pref_connectivity_bluetooth", false);
		saved_pref_connectivity_paired = preferences.getBoolean("saved_pref_connectivity_paired", false);
		saved_pref_service = preferences.getBoolean("saved_pref_service", false);
		saved_pref_sms = preferences.getBoolean("saved_pref_sms", false);
		saved_pref_sms_type_value = preferences.getString("saved_pref_sms_type_value", PREFS_DEFAULT);
		saved_pref_sms_type_entry = preferences.getString("saved_pref_sms_type_entry", PREFS_DEFAULT);
		saved_pref_sms_time_value = preferences.getString("saved_pref_sms_time_value", PREFS_DEFAULT);
		saved_pref_sms_time_entry = preferences.getString("saved_pref_sms_time_entry", PREFS_DEFAULT);
		saved_pref_sms_loop_value = preferences.getString("saved_pref_sms_loop_value", PREFS_DEFAULT);
		saved_pref_sms_loop_entry = preferences.getString("saved_pref_sms_loop_entry", PREFS_DEFAULT);
		saved_pref_sms_color = preferences.getString("saved_pref_sms_color", PREFS_DEFAULT);
		saved_pref_phone = preferences.getBoolean("saved_pref_phone", false);
		saved_pref_phone_type_value = preferences.getString("saved_pref_phone_type_value", PREFS_DEFAULT);
		saved_pref_phone_type_entry = preferences.getString("saved_pref_phone_type_entry", PREFS_DEFAULT);
		saved_pref_phone_time_value = preferences.getString("saved_pref_phone_time_value", PREFS_DEFAULT);
		saved_pref_phone_time_entry = preferences.getString("saved_pref_phone_time_entry", PREFS_DEFAULT);
		saved_pref_phone_loop_value = preferences.getString("saved_pref_phone_loop_value", PREFS_DEFAULT);
		saved_pref_phone_loop_entry = preferences.getString("saved_pref_phone_loop_entry", PREFS_DEFAULT);
		saved_pref_phone_color = preferences.getString("saved_pref_phone_color", PREFS_DEFAULT);
		saved_pref_connectivity_paired_value = preferences.getString("saved_pref_connectivity_paired_value", PREFS_DEFAULT);
		saved_pref_connectivity_paired_entry = preferences.getString("saved_pref_connectivity_paired_entry", PREFS_DEFAULT);
		saved_pref_repeat_value = preferences.getString("saved_pref_repeat_value", PREFS_DEFAULT);
		saved_pref_repeat_entry = preferences.getString("saved_pref_repeat_entry", PREFS_DEFAULT);
	}
}

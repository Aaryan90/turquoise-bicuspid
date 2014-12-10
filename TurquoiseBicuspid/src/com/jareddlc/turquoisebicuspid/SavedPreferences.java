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
    public boolean saved_preference_switch_bluetooth;
    public boolean saved_preference_list_paired;
    public boolean saved_preference_checkbox_service;
    public boolean saved_preference_checkbox_sms;
    public boolean saved_preference_checkbox_phone;
    public String saved_preference_list_paired_value;
    public String saved_preference_list_paired_entry;
    public String saved_preference_list_sms_type_value;
    public String saved_preference_list_sms_type_entry;
    public String saved_preference_list_sms_time_value;
    public String saved_preference_list_sms_time_entry;
    public String saved_preference_list_sms_loop_value;
    public String saved_preference_list_sms_loop_entry;
    public String saved_preference_list_phone_type_value;
    public String saved_preference_list_phone_type_entry;
    public String saved_preference_list_phone_time_value;
    public String saved_preference_list_phone_time_entry;
    public String saved_preference_list_phone_loop_value;
    public String saved_preference_list_phone_loop_entry;
    public String saved_preference_list_repeat_value;
    public String saved_preference_list_repeat_entry;
    public String saved_preference_color_sms;
    public String saved_preference_color_phone;

    public SavedPreferences(Context context) {
        this.load(context);
    }

    public void load(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        //Editor editor = preferences.edit();
        Log.d(LOG_TAG, "Loading saved preferences");
        saved_preference_switch_bluetooth = preferences.getBoolean("saved_preference_switch_bluetooth", false);
        saved_preference_list_paired = preferences.getBoolean("saved_preference_list_paired", false);
        saved_preference_checkbox_service = preferences.getBoolean("saved_preference_checkbox_service", false);
        saved_preference_checkbox_sms = preferences.getBoolean("saved_preference_checkbox_sms", false);
        saved_preference_checkbox_phone = preferences.getBoolean("saved_preference_checkbox_phone", false);
        saved_preference_list_paired_value = preferences.getString("saved_preference_list_paired_value", PREFS_DEFAULT);
        saved_preference_list_paired_entry = preferences.getString("saved_preference_list_paired_entry", PREFS_DEFAULT);
        saved_preference_list_sms_type_value = preferences.getString("saved_preference_list_sms_type_value", PREFS_DEFAULT);
        saved_preference_list_sms_type_entry = preferences.getString("saved_preference_list_sms_type_entry", PREFS_DEFAULT);
        saved_preference_list_sms_time_value = preferences.getString("saved_preference_list_sms_time_value", PREFS_DEFAULT);
        saved_preference_list_sms_time_entry = preferences.getString("saved_preference_list_sms_time_entry", PREFS_DEFAULT);
        saved_preference_list_sms_loop_value = preferences.getString("saved_preference_list_sms_loop_value", PREFS_DEFAULT);
        saved_preference_list_sms_loop_entry = preferences.getString("saved_preference_list_sms_loop_entry", PREFS_DEFAULT);
        saved_preference_color_sms = preferences.getString("saved_preference_color_sms", PREFS_DEFAULT);
        saved_preference_list_phone_type_value = preferences.getString("saved_preference_list_phone_type_value", PREFS_DEFAULT);
        saved_preference_list_phone_type_entry = preferences.getString("saved_preference_list_phone_type_entry", PREFS_DEFAULT);
        saved_preference_list_phone_time_value = preferences.getString("saved_preference_list_phone_time_value", PREFS_DEFAULT);
        saved_preference_list_phone_time_entry = preferences.getString("saved_preference_list_phone_time_entry", PREFS_DEFAULT);
        saved_preference_list_phone_loop_value = preferences.getString("saved_preference_list_phone_loop_value", PREFS_DEFAULT);
        saved_preference_list_phone_loop_entry = preferences.getString("saved_preference_list_phone_loop_entry", PREFS_DEFAULT);
        saved_preference_color_phone = preferences.getString("saved_preference_color_phone", PREFS_DEFAULT);
        saved_preference_list_repeat_value = preferences.getString("saved_preference_list_repeat_value", PREFS_DEFAULT);
        saved_preference_list_repeat_entry = preferences.getString("saved_preference_list_repeat_entry", PREFS_DEFAULT);
    }
}

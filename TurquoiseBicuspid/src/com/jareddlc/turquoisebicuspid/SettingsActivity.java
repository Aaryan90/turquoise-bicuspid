package com.jareddlc.turquoisebicuspid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.content.LocalBroadcastManager;
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
        // debug data
        private static final String LOG_TAG = "TurquoiseBicuspid:SettingsFragment";

        // preferences
        private static SwitchPreference preference_switch_bluetooth;
        private static CheckBoxPreference preference_checkbox_connect;
        private static CheckBoxPreference preference_checkbox_service;
        private static CheckBoxPreference preference_checkbox_sms;
        private static CheckBoxPreference preference_checkbox_phone;
        private static ListPreference preference_list_paired;
        private static ListPreference preference_list_sms_type;
        private static ListPreference preference_list_sms_time;
        private static ListPreference preference_list_sms_loop;
        private static ListPreference preference_list_phone_type;
        private static ListPreference preference_list_phone_time;
        private static ListPreference preference_list_phone_loop;
        private static ListPreference preference_list_repeat;
        private static ColorPickerPreference preference_color_sms;
        private static ColorPickerPreference preference_color_phone;
        private static Preference preference_scan;
        private static Preference preference_clear;
        private static Preference preference_test;

        // private static objects
        private static Handler mHandler;
        private static BluetoothLeService bluetoothLeService;

        private String mDeviceName;
        private String mDeviceAddress;

        @SuppressLint("HandlerLeak")
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // load preferences from xml
            addPreferencesFromResource(R.xml.preferences);

            // load saved preferences
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            final Editor editor = preferences.edit();
            final SavedPreferences sPrefs = new SavedPreferences(getActivity());

            // setup message handler
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    Log.d(LOG_TAG, "handleMessage: "+msg.getData());
                    String bluetoothMessage = msg.getData().getString("bluetooth");
                    String bluetoothDevice = msg.getData().getString("bluetoothDevice");
                    if(bluetoothMessage != null && !bluetoothMessage.isEmpty()) {
                        if(bluetoothMessage.equals("isEnabled")) {
                            CharSequence text = "Bluetooth Enabled";
                            Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
                            SettingsFragment.this.restorePreferences(sPrefs);                
                            preference_switch_bluetooth.setChecked(true);
                            preference_list_paired.setEnabled(true);
                        }
                        if(bluetoothMessage.equals("isEnabledFailed")) {
                            CharSequence text = "Failed";
                            Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
                            preference_switch_bluetooth.setChecked(false);
                        }
                        if(bluetoothMessage.equals("isConnected")) {
                            Log.d(LOG_TAG, "Bluetooth Connected");
                            CharSequence text = "Bluetooth Connected";
                            Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
                            preference_checkbox_connect.setChecked(true);
                            preference_checkbox_service.setEnabled(true);
                        }
                        if(bluetoothMessage.equals("isDisconnected")) {
                            Log.d(LOG_TAG, "Bluetooth Disconnected");
                            CharSequence text = "Bluetooth Disconnected";
                            Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
                            preference_checkbox_connect.setChecked(false);
                        }
                        if(bluetoothMessage.equals("isConnectedFailed")) {
                            Log.d(LOG_TAG, "Bluetooth Connected Failed");
                            CharSequence text = "Bluetooth Connected failed";
                            Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
                            preference_checkbox_connect.setChecked(false);
                            preference_checkbox_service.setEnabled(true);
                            preference_checkbox_service.setChecked(false);
                        }
                        if(bluetoothMessage.equals("scanStopped")) {
                            Log.d(LOG_TAG, "Bluetooth scanning done");
                            preference_list_paired.setEntries(BluetoothLeService.getEntries());
                            preference_list_paired.setEntryValues(BluetoothLeService.getEntryValues());
                            CharSequence text = "Scanning complete. Please select device";
                            Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(bluetoothDevice != null && !bluetoothDevice.isEmpty()) {
                        String[] sDevice = bluetoothDevice.split(",");
                        String sDeviceName = sDevice[0];
                        String sDeviceAddress = sDevice[1];
                        Log.d(LOG_TAG, "Bluetooth device name: "+sDeviceName+" address: "+sDeviceAddress);
                    }
                }
            };

            // initialize BluetoothLE
            final ServiceConnection mServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder service) {
                    bluetoothLeService = ((BluetoothLeService.LocalBinder)service).getService();
                    if(!bluetoothLeService.initialize()) {
                        Log.e(LOG_TAG, "Unable to initialize BluetoothLE");
                    }
                    bluetoothLeService.setHandler(mHandler);
                    
                    if(BluetoothLeService.isEnabled) {
                        preference_switch_bluetooth.setChecked(true);
                        editor.putBoolean("saved_preference_switch_bluetooth", true);
                        editor.commit();
                        SettingsFragment.this.restorePreferences(sPrefs);
                        if(!BluetoothLeService.isConnected) {
                            preference_checkbox_service.setChecked(false);
                        }
                    }
                    else {
                        preference_switch_bluetooth.setChecked(false);
                        editor.putBoolean("saved_preference_switch_bluetooth", false);
                        editor.commit();
                    }
                    // Automatically connects to the device upon successful start-up initialization.
                    //bluetoothLeService.connect(mDeviceAddress);
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    bluetoothLeService = null;
                }
            };
            Intent gattServiceIntent = new Intent(getActivity(), BluetoothLeService.class);
            getActivity().bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

            // UI listeners
            preference_switch_bluetooth = (SwitchPreference) getPreferenceManager().findPreference("preference_switch_bluetooth");
            preference_switch_bluetooth.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean)newValue) {
                        preference_list_paired.setEnabled(false);
                        preference_switch_bluetooth.setChecked(false);
                        bluetoothLeService.enableBluetooth();
                        CharSequence text = "Enabling...";
                        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
                        editor.putBoolean("saved_preference_switch_bluetooth", true);
                        editor.commit();
                    }
                    else {
                        bluetoothLeService.disableBluetooth();
                        editor.putBoolean("saved_preference_switch_bluetooth", false);
                        editor.commit();
                    }
                    return true;
                }
            });

            preference_scan = (Preference) getPreferenceManager().findPreference("preference_list_scan");
            preference_scan.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    CharSequence text = "Scanning...";
                    Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
                    bluetoothLeService.scanLeDevice();
                    return true;
                }
            });

            preference_list_paired = (ListPreference) getPreferenceManager().findPreference("preference_list_paired");
            preference_list_paired.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    BluetoothLeService.setEntries();
                    preference_list_paired.setEntries(BluetoothLeService.getEntries());
                    preference_list_paired.setEntryValues(BluetoothLeService.getEntryValues());
                    return true;
                }
            });
            preference_list_paired.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    BluetoothLeService.setDevice(newValue.toString());
                    int index = preference_list_paired.findIndexOfValue(newValue.toString());
                    CharSequence[] entries = preference_list_paired.getEntries();
                    editor.putString("saved_preference_list_paired_value", newValue.toString());
                    editor.putString("saved_preference_list_paired_entry", entries[index].toString());
                    editor.commit();
                    mDeviceAddress = newValue.toString();
                    mDeviceName = entries[index].toString();
                    preference_checkbox_connect.setSummaryOff("Click to connect ("+mDeviceName+")");
                    preference_list_paired.setSummary(entries[index].toString());
                    return true;
                }
            });

            preference_checkbox_connect = (CheckBoxPreference) getPreferenceManager().findPreference("preference_checkbox_connect");
            preference_checkbox_connect.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean)newValue) {
                        bluetoothLeService.connect(mDeviceAddress);
                        return false;
                    }
                    else {
                        bluetoothLeService.disconnect();
                        return true;
                    }
                }
            });

            preference_checkbox_service = (CheckBoxPreference) getPreferenceManager().findPreference("preference_checkbox_service");
            preference_checkbox_service.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean)newValue) {
                        getActivity().startService(new Intent(getActivity(),SettingsService.class));
                        editor.putBoolean("saved_preference_checkbox_service", true);
                        editor.commit();
                    }
                    else {
                        getActivity().stopService(new Intent(getActivity(),SettingsService.class));
                        bluetoothLeService.disconnect();
                        editor.putBoolean("saved_preference_checkbox_service", false);
                        editor.commit();
                    }
                    return true;
                }
            });

            preference_clear = (Preference) getPreferenceManager().findPreference("preference_clear");
            preference_clear.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    bluetoothLeService.send("blink", "3", "50", "-1", "ffffff");
                    return true;
                }
            });

            preference_test = (Preference) getPreferenceManager().findPreference("preference_test");
            preference_test.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    bluetoothLeService.send("blink", "3", "250", "-1", "ffffff");
                    return true;
                }
            });

            preference_checkbox_sms = (CheckBoxPreference) getPreferenceManager().findPreference("preference_checkbox_sms");
            preference_checkbox_sms.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean)newValue) {
                        SettingsService.setSms(true);
                        editor.putBoolean("saved_preference_checkbox_sms", true);
                        editor.commit();
                    }
                    else {
                        SettingsService.setSms(false);
                        editor.putBoolean("saved_preference_checkbox_sms", false);
                        editor.commit();
                    }
                    return true;
                }
            });
            preference_list_sms_type = (ListPreference) getPreferenceManager().findPreference("preference_list_sms_type");
            preference_list_sms_type.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int index = preference_list_sms_type.findIndexOfValue(newValue.toString());
                    CharSequence[] entries = preference_list_sms_type.getEntries();
                    editor.putString("saved_preference_list_sms_type_value", newValue.toString());
                    editor.putString("saved_preference_list_sms_type_entry", entries[index].toString());
                    editor.commit();
                    bluetoothLeService.send(newValue.toString(), preference_list_sms_loop.getValue(), preference_list_sms_time.getValue(), "-1", preference_color_sms.getHexValue());
                    notifyService();
                    return true;
                }
            });
            preference_list_sms_time = (ListPreference) getPreferenceManager().findPreference("preference_list_sms_time");
            preference_list_sms_time.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int index = preference_list_sms_time.findIndexOfValue(newValue.toString());
                    CharSequence[] entries = preference_list_sms_time.getEntries();
                    editor.putString("saved_preference_list_sms_time_value", newValue.toString());
                    editor.putString("saved_preference_list_sms_time_entry", entries[index].toString());
                    editor.commit();
                    bluetoothLeService.send(preference_list_sms_type.getValue(), preference_list_sms_loop.getValue(), newValue.toString(), "-1", preference_color_sms.getHexValue());
                    notifyService();
                    return true;
                }
            });
            preference_list_sms_loop = (ListPreference) getPreferenceManager().findPreference("preference_list_sms_loop");
            preference_list_sms_loop.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int index = preference_list_sms_loop.findIndexOfValue(newValue.toString());
                    CharSequence[] entries = preference_list_sms_loop.getEntries();
                    editor.putString("saved_preference_list_sms_loop_value", newValue.toString());
                    editor.putString("saved_preference_list_sms_loop_entry", entries[index].toString());
                    editor.commit();
                    bluetoothLeService.send(preference_list_sms_type.getValue(), newValue.toString(), preference_list_sms_time.getValue(), "-1", preference_color_sms.getHexValue());
                    notifyService();
                    return true;
                }
            });

            preference_color_sms = (ColorPickerPreference) getPreferenceManager().findPreference("preference_color_sms");
            preference_color_sms.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    editor.putString("saved_preference_color_sms", Integer.toHexString((Integer)newValue).substring(2));
                    editor.commit();
                    bluetoothLeService.send(preference_list_sms_type.getValue(), preference_list_sms_loop.getValue(), preference_list_sms_time.getValue(), "-1", Integer.toHexString((Integer)newValue).substring(2));
                    notifyService();
                    return true;
                }
            });

            preference_checkbox_phone = (CheckBoxPreference) getPreferenceManager().findPreference("preference_checkbox_phone");
            preference_checkbox_phone.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean)newValue) {
                        SettingsService.setPhone(true);
                        editor.putBoolean("saved_preference_checkbox_phone", true);
                        editor.commit();
                    }
                    else {
                        SettingsService.setPhone(false);
                        editor.putBoolean("saved_preference_checkbox_phone", false);
                        editor.commit();
                    }
                    return true;
                }
            });
            preference_list_phone_type = (ListPreference) getPreferenceManager().findPreference("preference_list_phone_type");
            preference_list_phone_type.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int index = preference_list_phone_type.findIndexOfValue(newValue.toString());
                    CharSequence[] entries = preference_list_phone_type.getEntries();
                    editor.putString("saved_preference_list_phone_type_value", newValue.toString());
                    editor.putString("saved_preference_list_phone_type_entry", entries[index].toString());
                    editor.commit();
                    bluetoothLeService.send(newValue.toString(), preference_list_phone_loop.getValue(), preference_list_phone_time.getValue(), "-1", preference_color_phone.getHexValue());
                    notifyService();
                    return true;
                }
            });
            preference_list_phone_time = (ListPreference) getPreferenceManager().findPreference("preference_list_phone_time");
            preference_list_phone_time.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int index = preference_list_phone_time.findIndexOfValue(newValue.toString());
                    CharSequence[] entries = preference_list_phone_time.getEntries();
                    editor.putString("saved_preference_list_phone_time_value", newValue.toString());
                    editor.putString("saved_preference_list_phone_time_entry", entries[index].toString());
                    editor.commit();
                    bluetoothLeService.send(preference_list_phone_type.getValue(), preference_list_phone_loop.getValue(), newValue.toString(), "-1", preference_color_phone.getHexValue());
                    notifyService();
                    return true;
                }
            });
            preference_list_phone_loop = (ListPreference) getPreferenceManager().findPreference("preference_list_phone_loop");
            preference_list_phone_loop.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int index = preference_list_phone_loop.findIndexOfValue(newValue.toString());
                    CharSequence[] entries = preference_list_phone_loop.getEntries();
                    editor.putString("saved_preference_list_phone_loop_value", newValue.toString());
                    editor.putString("saved_preference_list_phone_loop_entry", entries[index].toString());
                    editor.commit();
                    bluetoothLeService.send(preference_list_phone_type.getValue(), newValue.toString(), preference_list_phone_time.getValue(), "-1", preference_color_phone.getHexValue());
                    notifyService();
                    return true;
                }
            });

            preference_color_phone = (ColorPickerPreference) getPreferenceManager().findPreference("preference_color_phone");
            preference_color_phone.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    editor.putString("saved_preference_color_phone", Integer.toHexString((Integer)newValue).substring(2));
                    editor.commit();
                    bluetoothLeService.send(preference_list_phone_type.getValue(), preference_list_phone_loop.getValue(), preference_list_phone_time.getValue(), "-1", Integer.toHexString((Integer)newValue).substring(2));
                    notifyService();
                    return true;
                }
            });

            preference_list_repeat = (ListPreference) getPreferenceManager().findPreference("preference_list_repeat");
            preference_list_repeat.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int index = preference_list_repeat.findIndexOfValue(newValue.toString());
                    CharSequence[] entries = preference_list_repeat.getEntries();
                    editor.putString("saved_preference_list_repeat_value", newValue.toString());
                    editor.putString("saved_preference_list_repeat_entry", entries[index].toString());
                    editor.commit();
                    notifyService();
                    return true;
                }
            });
        }

        public void notifyService() {
            Log.d(LOG_TAG, "Broadcasting setPreferences");
            Intent msg = new Intent("setPreferences");
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(msg);
        }

        public void restorePreferences(SavedPreferences sPrefs) {
            Log.d(LOG_TAG, "Restore paired device: "+sPrefs.saved_preference_list_paired_value+":"+sPrefs.saved_preference_list_paired_entry);
            BluetoothLeService.setEntries();
            if(sPrefs.saved_preference_list_paired_value != "DEFAULT") {
                BluetoothLeService.setDevice(sPrefs.saved_preference_list_paired_value);
                mDeviceAddress = sPrefs.saved_preference_list_paired_value;
                preference_list_paired.setSummary(sPrefs.saved_preference_list_paired_entry);
                preference_checkbox_connect.setSummaryOff("Click to connect ("+sPrefs.saved_preference_list_paired_entry+")");
            }
            preference_list_paired.setEntries(BluetoothLeService.getEntries());
            preference_list_paired.setEntryValues(BluetoothLeService.getEntryValues());
        }
     }
}

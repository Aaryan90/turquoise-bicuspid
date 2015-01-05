package com.jareddlc.turquoisebicuspid;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class SettingsService extends Service {
    // debug data
    private static final String LOG_TAG = "TurquoiseBicuspid:SettingsService";

    private static boolean smsEnabled = true;
    private static boolean phoneEnabled = true;
    private CallListener callListener;
    private SmsListener smsListener;
    private TelephonyManager telephony;
    private IntentFilter smsFilter;

    private String preference_sms_type;
    private String preference_sms_time;
    private String preference_sms_number;
    private String preference_sms_color;
    private String preference_phone_type;
    private String preference_phone_time;
    private String preference_phone_number;
    private String preference_phone_color;
    private String preference_repeat;

    private Handler mHandler;
    private static Bluetooth bluetooth;

    public static void setSms(boolean value) {
        Log.d(LOG_TAG, "SMS: "+value);
        smsEnabled = value;
    }

    public static void setPhone(boolean value) {
        Log.d(LOG_TAG, "Phone: "+value);
        phoneEnabled = value;
    }

    public void setPreferences(SavedPreferences sPrefs) {
        preference_sms_type = sPrefs.saved_preference_list_sms_type_value;
        preference_sms_time = sPrefs.saved_preference_list_sms_time_value;
        preference_sms_number = sPrefs.saved_preference_list_sms_number_value;
        preference_sms_color = sPrefs.saved_preference_color_sms;
        preference_phone_type = sPrefs.saved_preference_list_phone_type_value;
        preference_phone_time = sPrefs.saved_preference_list_phone_time_value;
        preference_phone_number = sPrefs.saved_preference_list_phone_number_value;
        preference_phone_color = sPrefs.saved_preference_color_phone;
        preference_repeat = sPrefs.saved_preference_list_repeat_value;
    }

    @SuppressLint("HandlerLeak")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service running", Toast.LENGTH_SHORT).show();
        Log.d(LOG_TAG, "Service running");

        // restore saved preferences
        final SavedPreferences sPrefs = new SavedPreferences(this);
        preference_sms_type = sPrefs.saved_preference_list_sms_type_value;
        preference_sms_time = sPrefs.saved_preference_list_sms_time_value;
        preference_sms_number = sPrefs.saved_preference_list_sms_number_value;
        preference_sms_color = sPrefs.saved_preference_color_sms;
        preference_phone_type = sPrefs.saved_preference_list_phone_type_value;
        preference_phone_time = sPrefs.saved_preference_list_phone_time_value;
        preference_phone_number = sPrefs.saved_preference_list_phone_number_value;
        preference_phone_color = sPrefs.saved_preference_color_phone;
        preference_repeat = sPrefs.saved_preference_list_repeat_value;

        // register broadcast events
         LocalBroadcastManager.getInstance(this).registerReceiver(smsReceiver, new IntentFilter("sms"));
         LocalBroadcastManager.getInstance(this).registerReceiver(phoneReceiver, new IntentFilter("phone"));
         LocalBroadcastManager.getInstance(this).registerReceiver(prefReceiver, new IntentFilter("setPreferences"));

        // setup message handler
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String bluetoothMsg = msg.getData().getString("bluetooth");
                if(bluetoothMsg.equals("isEnabled")) {
                    CharSequence text = "Bluetooth Enabled";
                    Toast.makeText(SettingsService.this, text, Toast.LENGTH_SHORT).show();
                }
                if(bluetoothMsg.equals("isConnected")) {
                    Log.d(LOG_TAG, "Bluetooth Connected");
                    CharSequence text = "Bluetooth Connected";
                    Toast.makeText(SettingsService.this, text, Toast.LENGTH_SHORT).show();
                }
                if(bluetoothMsg.equals("isConnectedFailed")) {
                    Log.d(LOG_TAG, "Bluetooth Connected Failed");
                    CharSequence text = "Bluetooth Connected failed";
                    Toast.makeText(SettingsService.this, text, Toast.LENGTH_SHORT).show();
                }
            }
        };

        // initialize Bluetooth
        bluetooth = new Bluetooth(mHandler);

        if(bluetooth.isEnabled) {
            bluetooth.getPaired();
            bluetooth.setDevice(sPrefs.saved_preference_list_paired_value);
            bluetooth.connectDevice();
        }

        // register listeners
        if(smsEnabled) {
            smsListener = new SmsListener();
            smsFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
            this.registerReceiver(smsListener, smsFilter);
            Log.d(LOG_TAG, "sms listening");
        }

        if(phoneEnabled) {
            callListener = new CallListener(getApplicationContext());
            telephony = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            telephony.listen(callListener, PhoneStateListener.LISTEN_CALL_STATE);
            Log.d(LOG_TAG, "phone listening");
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show();
        Log.d(LOG_TAG, "Service stopped");
        bluetooth.disconnectDevice();

        // unregister listeners
        if(smsEnabled) {
            this.unregisterReceiver(smsListener);
        }
        
        if(phoneEnabled) {
            telephony.listen(callListener, PhoneStateListener.LISTEN_NONE);
            callListener.destroy();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return null;
    }

    private BroadcastReceiver smsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            String sender = intent.getStringExtra("sender");
            Log.d(LOG_TAG, "Recieved SMS message: "+sender+" - "+message);
            bluetooth.send(preference_sms_type, preference_sms_number, preference_sms_time, preference_repeat, preference_sms_color);
        }
    };

    private BroadcastReceiver phoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String sender = intent.getStringExtra("sender");
            Log.d(LOG_TAG, "Recieved PHONE: "+sender);
            bluetooth.send(preference_phone_type, preference_phone_number, preference_phone_time, preference_repeat, preference_phone_color);
        }
    };

    private BroadcastReceiver prefReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "Recieved PREF: ");
            SavedPreferences sPrefs = new SavedPreferences(context);
            setPreferences(sPrefs);
        }
    };
}

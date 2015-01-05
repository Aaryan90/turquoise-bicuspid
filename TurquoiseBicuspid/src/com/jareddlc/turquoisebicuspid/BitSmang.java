package com.jareddlc.turquoisebicuspid;

import android.util.Log;

public class BitSmang {
    /*
    Type:
        Blink: 0
        Pulse: 1
        On: 2
        Off: 3
    Number:
        1: 0
        2: 1
        3: 2
        5: 3
    Time:
        50: 0
        100: 1
        250: 2
        500: 3
    Repeat:
        0: 0
        5000: 1
        15000: 2
        30000: 3
    */
    //00-00-00-00-00000000-00000000-00000000
    // debug data
    private static final String LOG_TAG = "TurquoiseBicuspid:BitSmang";

    public static byte api;
    public static byte red;
    public static byte green;
    public static byte blue;

    public static  byte[] toByte(String type, String number, String time, String repeat, String rgb) {

        api = 0;
        red = 0;
        green = 0;
        blue = 0;

        api |= Integer.parseInt(type);
        api <<= 2;
        api |= Integer.parseInt(number);
        api <<= 2;
        api |= Integer.parseInt(time);
        api <<= 2;
        api |= Integer.parseInt(repeat);
        Log.d(LOG_TAG, "API:"+api+", Binary:"+Integer.toBinaryString(api));

        String r = rgb.substring(0,2);
        String g = rgb.substring(2,4);
        String b = rgb.substring(4,6);

        red |= Integer.parseInt(r, 16);
        green |= Integer.parseInt(g, 16);
        blue |= Integer.parseInt(b, 16);
        Log.d(LOG_TAG, "R:"+Integer.toString(red & 0xff)+", Binary:"+Integer.toBinaryString(red));
        Log.d(LOG_TAG, "G:"+Integer.toString(green & 0xff)+", Binary:"+Integer.toBinaryString(green));
        Log.d(LOG_TAG, "B:"+Integer.toString(blue & 0xff)+", Binary:"+Integer.toBinaryString(blue));

        byte[] bitsmang = new byte[]{api, red, green, blue};
        Log.d(LOG_TAG, "bitsmang:"+bitsmang.toString());

        return bitsmang;
    }
}

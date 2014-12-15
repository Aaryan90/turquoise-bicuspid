package com.jareddlc.turquoisebicuspid;

import android.util.Log;

public class BitSmang {
    /*
    Type:
        Blink: 0
        Pulse: 1
        On: 2
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

    public static byte bits;
    public static byte red;
    public static byte green;
    public static byte blue;

    public static  byte[] toByte(String type, String number, String time, String repeat, String rgb) {

        bits = 0;
        red = 0;
        green = 0;
        blue = 0;

        bits |= Integer.parseInt(type);
        bits <<= 2;
        bits |= Integer.parseInt(number);
        bits <<= 2;
        bits |= Integer.parseInt(time);
        bits <<= 2;
        bits |= Integer.parseInt(repeat);
        Log.d(LOG_TAG, "bits:"+bits);

        String r = rgb.substring(0,2);
        String g = rgb.substring(2,4);
        String b = rgb.substring(4,6);

        red |= Integer.parseInt(r, 16);
        green |= Integer.parseInt(g, 16);
        blue |= Integer.parseInt(b, 16);
        Log.d(LOG_TAG, "R:"+Integer.toString(red & 0xff));
        Log.d(LOG_TAG, "G:"+Integer.toString(green & 0xff));
        Log.d(LOG_TAG, "B:"+Integer.toString(blue & 0xff));

        byte[] bitsmang = new byte[]{bits, red, green, blue};
        Log.d(LOG_TAG, "bitsmang:"+bitsmang.toString());

        return bitsmang;
    }
}

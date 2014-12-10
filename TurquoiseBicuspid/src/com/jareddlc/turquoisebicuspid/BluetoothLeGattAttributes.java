package com.jareddlc.turquoisebicuspid;

import java.util.HashMap;

public class BluetoothLeGattAttributes {
    private static HashMap<String, String> attributes = new HashMap<String, String>();
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";    
    public static String HM_10_CONF = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public static String HM_RX_TX = "0000ffe1-0000-1000-8000-00805f9b34fb";

    static {
        // services.
        attributes.put("0000ffe0-0000-1000-8000-00805f9b34fb", "TurquoiseBicuspid");
        attributes.put("00001800-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // characteristics.
        attributes.put(HM_RX_TX,"RX/TX data");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Turquoise Bicuspid");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}

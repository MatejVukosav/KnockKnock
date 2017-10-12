package com.things.android.vuki.knockknock;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

/**
 * Created by mvukosav
 */
public class WifiHelper {

    //    String networkSSID = "Your Network SSID here";
//    String networkPasskey = "YourNetworkPasswordHere";
    String networkSSID = "stinjevac";
    String networkPasskey = "ivan24071999";

    private void connectToWifit( Context context ) {

        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = "\"" + networkSSID + "\"";
        wifiConfiguration.preSharedKey = "\"" + networkPasskey + "\"";

        WifiManager manager = (WifiManager) context.getSystemService( context.WIFI_SERVICE );
        manager.addNetwork( wifiConfiguration );
    }

}
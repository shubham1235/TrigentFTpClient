package com.example.shubham_v.TrigentFTPclient;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.List;

/**
 * Created by shubham_v on 06-06-2017.
 */

public class WifiP2PConfiguration {

    static Boolean Wifi_Connection_Status_Flag = false;
    static WifiManager wifiManager;

    public static boolean ConnectionWithWifi(Context context) {

        android.net.wifi.WifiConfiguration conf = new android.net.wifi.WifiConfiguration();
        conf.SSID = String.format("\"%s\"", "WTBFTP_wifi");
        conf.preSharedKey = "\"" + "shubhamv" + "\"";
        conf.priority = 1000;
        conf.status = android.net.wifi.WifiConfiguration.Status.ENABLED;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int networkId = wifiManager.addNetwork(conf);


        List<ScanResult> list = wifiManager.getScanResults();
        for (ScanResult i : list) {
            if (i.SSID != null && i.SSID.toString().equals("WTBFTP_wifi")) {
                try {
                    wifiManager.disconnect();
                    if (networkId != -1) {
                        wifiManager.enableNetwork(networkId, true);
                    }

                    Wifi_Connection_Status_Flag = wifiManager.reconnect();

                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return Wifi_Connection_Status_Flag;
    }

    public static boolean wifiDisconect() {
        return wifiManager.disconnect();
    }

}

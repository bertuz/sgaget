package org.altervista.bertuz83.sgaget.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;
import org.altervista.bertuz83.sgaget.service.ServiceTracking;

/**
 * User: bertuz
 * Project: sgaget
 *
 * receiver per l'ascolto del broadcast di sistema riguardante lo status del WiFi.
 *
 * @see org.altervista.bertuz83.sgaget.service.ServiceTracking
 */
public class ReceiverWiFiChanged extends BroadcastReceiver {
    ControlReceiverWiFiChanged service;

    public ReceiverWiFiChanged(ControlReceiverWiFiChanged service){
        this.service= service;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int extraWifiState= intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
        if((extraWifiState == WifiManager.WIFI_STATE_DISABLED) ||(extraWifiState == WifiManager.WIFI_STATE_DISABLING)){
            service.onWiFiDisabled();
        }
    }
}

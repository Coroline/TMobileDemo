package com.example.tmobilenetworkdemo.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * BroadcastReceiver about whether the hotspot AP is turned on
 */
public class HotSpotIntentReceiver extends BroadcastReceiver {
    private final static String TAG = HotSpotIntentReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"Received intent");
        if (intent != null) {
            final String action = intent.getAction();
            if (intent.getAction().equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {
                int apState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                if (apState == 13) {
                    // Hotspot AP is enabled
                    Log.d(TAG, "ap turn on");
                } else {
                    // Hotspot AP is disabled/not ready
                    Log.d(TAG, "ap turn off");
                }
            }
        }

    }
}

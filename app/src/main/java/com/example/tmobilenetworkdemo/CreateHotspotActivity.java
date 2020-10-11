package com.example.tmobilenetworkdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.tmobilenetworkdemo.Wifi.WifiAdmin;
import com.skyfishjy.library.BuildConfig;

import java.lang.reflect.Method;

public class CreateHotspotActivity extends AppCompatActivity {

    private Button createHotspot;
    private EditText SSID;
    private EditText password;
    private WifiManager wifiManager;
    private WifiConfiguration wifiConfiguration;
    private boolean mIsConnectingWifi=false;
    private boolean mIsFirstReceiveConnected=false;
    private WifiAdmin mWifiAdmin;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_hotspot);
        createHotspot = findViewById(R.id.createHotspot);
        SSID = findViewById((R.id.ssid));
        password = findViewById(R.id.password);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        wifiConfiguration = new WifiConfiguration();
        mWifiAdmin=new WifiAdmin(this);

        requestPermissions();
        createHotspot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int wcgID = mWifiAdmin.connectWifi(SSID.getText().toString(), password.getText().toString(), 1);
                boolean start = startHotSpot(wcgID != -1);
                if(start)
                    Toast.makeText(getApplicationContext(), "Successfully create a hotspot.", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(), "Fail to create a hotspot.", Toast.LENGTH_LONG).show();
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPermissions() {
        if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    public boolean startHotSpot(boolean enable) {
        wifiManager.setWifiEnabled(false);
        Method[] mMethods = wifiManager.getClass().getDeclaredMethods();
        for (Method mMethod : mMethods) {
            if (mMethod.getName().equals("setWifiApEnabled")) {
                try {
                    mMethod.invoke(wifiManager, null, enable);
                    return true;
                } catch (Exception ex) {
                }
                break;
            }
        }
        return false;
    }

}

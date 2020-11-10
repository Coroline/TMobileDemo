package com.example.tmobilenetworkdemo;

import android.Manifest;
import android.content.Intent;
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
import androidx.core.content.ContextCompat;
import androidx.annotation.RequiresPermission;

import com.example.tmobilenetworkdemo.Wifi.MyOnStartTetheringCallback;
import com.example.tmobilenetworkdemo.Wifi.WifiAdmin;
import com.example.tmobilenetworkdemo.Wifi.WifiHotUtil;


public class CreateHotspotActivity extends AppCompatActivity {

    private Button createHotspot;
    private EditText SSID;
    private EditText password;
    private WifiManager wifiManager;
    private WifiConfiguration wifiConfiguration;
    private boolean mIsConnectingWifi=false;
    private boolean mIsFirstReceiveConnected=false;
    private WifiAdmin mWifiAdmin;
    private WifiHotUtil mWifiHotUtil;

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
        mWifiAdmin = new WifiAdmin(getApplicationContext());
        mWifiHotUtil = WifiHotUtil.getInstance(getApplicationContext());


        requestPermissions();
        createHotspot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyOnStartTetheringCallback callback = new MyOnStartTetheringCallback() {
                    @Override
                    public void onTetheringStarted() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Successfully create a hotspot." , Toast.LENGTH_LONG).show();
                                Intent i = new Intent(CreateHotspotActivity.this, CreatedHotspotInformationActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("hotspotName", SSID.getText().toString());
                                i.putExtra("data", bundle);
                                startActivity(i);
                            }
                        });
                    }

                    @Override
                    public void onTetheringFailed() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Fail to create a hotspot." , Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                };
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    mWifiHotUtil.turnOnWifiApAsync(SSID.getText().toString(), password.getText().toString(), WifiHotUtil.WifiSecurityType.WIFICIPHER_WPA2, callback);
                } else {
                    mWifiHotUtil.hotspotOreo(true, callback);
                }
//                Intent i = new Intent(CreateHotspotActivity.this, CreatedHotspotInformationActivity.class);
//                Bundle bundle = new Bundle();
//                bundle.putString("hotspotName", SSID.getText().toString());
//                i.putExtra("data", bundle);
//                startActivity(i);
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
}

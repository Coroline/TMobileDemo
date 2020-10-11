package com.example.tmobilenetworkdemo;

import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tmobilenetworkdemo.Wifi.WifiAdmin;

import java.util.List;

public class ConnectHotspotActivity extends AppCompatActivity {

    private Button scan;
    private WifiAdmin mWifiAdmin;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_hotspot);
        scan = findViewById(R.id.scan);
        mWifiAdmin = new WifiAdmin(this);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<ScanResult> scanResult = mWifiAdmin.getScanResultList();
                System.out.println(scanResult);
            }
        });
    }
}

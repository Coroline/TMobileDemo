package com.example.tmobilenetworkdemo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Service;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.tmobilenetworkdemo.Lib.GPSTracking;
import com.example.tmobilenetworkdemo.Lib.NetworkInformationManager;
import com.example.tmobilenetworkdemo.Receiver.HotSpotIntentReceiver;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {

    private Button create;
    private Button connect;
    static final int MY_PERMISSIONS_MANAGE_WRITE_SETTINGS = 100;
    private GPSTracking locationService;
    private ServiceConnection conn;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        create = findViewById(R.id.create);
        connect = findViewById(R.id.connect);
        settingPermission();
        requestPermissions();
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), CreateHotspotActivity.class);
                startActivity(i);
            }
        });
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), ConnectHotspotActivity.class);
                startActivity(i);
            }
        });
        IntentFilter intentFilter = new IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGEDD");
        getApplicationContext().registerReceiver(new HotSpotIntentReceiver(), intentFilter);


        // Location update service
        Intent serviceIntent = new Intent(this, GPSTracking.class);
        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                System.out.println("---------------------------------------");
                GPSTracking.MyBinder binder = (GPSTracking.MyBinder) service;
                locationService = binder.getService();
//                Location loc = locationService.getLocation();
//                System.out.println(loc.getLatitude() + " | " + loc.getLongitude());
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                locationService = null;
            }
        };
        getApplicationContext().bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);


        Timer timer = new Timer();
        timer.schedule(new MyTask(), 0, 5000);
    }


    class MyTask extends TimerTask {

        @Override
        public void run() {
            System.out.println(GPSTracking.lat + " " + GPSTracking.lng);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_NETWORK_STATE}, 1);
        }
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
//        }
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 1);
//        }
        if (Build.VERSION.SDK_INT >= 21) {
            getApplicationContext();
            UsageStatsManager mUsageStatsManager = (UsageStatsManager) getApplicationContext().getSystemService(USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time);

            if (stats == null || stats.isEmpty()) {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                getApplicationContext().startActivity(intent);
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private void settingPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d("canwrite", Boolean.toString(Settings.System.canWrite(getApplicationContext())));
            if (!Settings.System.canWrite(getApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, MY_PERMISSIONS_MANAGE_WRITE_SETTINGS);
            }
        }
    }
}
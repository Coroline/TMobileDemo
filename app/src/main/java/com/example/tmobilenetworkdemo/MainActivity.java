package com.example.tmobilenetworkdemo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.tmobilenetworkdemo.Lib.GPSTracking;
import com.example.tmobilenetworkdemo.Lib.NetworkInformationManager;
import com.example.tmobilenetworkdemo.Receiver.HotSpotIntentReceiver;

import java.lang.reflect.Method;
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
    private LinearLayout homePage;
    private LinearLayout account;
    private final static String TAG = MainActivity.class.getSimpleName();

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Window _window = getWindow();
//        WindowManager.LayoutParams params = _window.getAttributes();
//        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE;
//        _window.setAttributes(params);

        create = findViewById(R.id.create);
        connect = findViewById(R.id.connect);
        homePage = findViewById(R.id.home_page);
        account = findViewById(R.id.account);
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
                showDialog(view);
            }
        });
        IntentFilter intentFilter = new IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGEDD");
        getApplicationContext().registerReceiver(new HotSpotIntentReceiver(), intentFilter);
        account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, AccountActivity.class);
                startActivity(i);
            }
        });


        // Location update service
        Intent serviceIntent = new Intent(this, GPSTracking.class);
        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
//                System.out.println("---------------------------------------");
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

//        Timer timer = new Timer();
//        timer.schedule(new MyTask(), 0, 5000);
    }

//    class MyTask extends TimerTask {
//        @Override
//        public void run() {
//            System.out.println(GPSTracking.lat + " " + GPSTracking.lng);
//        }
//    }


//    @Override
//    protected void onStart() {
//        super.onStart();
////        if (Build.VERSION.SDK_INT < 19 || !checkDeviceHasNavigationBar(this)) {
////            return;
////        } else {
////            // 主要就是通过设置特定的属性，来控制Navigationbar的显示，有兴趣的同学可以去查查相关介绍
////            int flag = (View.SYSTEM_UI_FLAG_VISIBLE
////                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
////            decorView.setSystemUiVisibility(flag);
////        }
//        if(Build.VERSION.SDK_INT >= 19 && !checkDeviceHasNavigationBar(this)) {
//            getParent().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//        }
//    }


    private boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {
            Log.w(TAG, e);
        }
        return hasNavigationBar;
    }

    public void showDialog(View view){
        LayoutInflater factory = LayoutInflater.from(this);
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        final View dialogView = factory.inflate(R.layout.fragment_connect_hotspot_parameter, null);
        final EditText connectDurationText = (EditText) dialogView.findViewById(R.id.bandwidth_duration);
        final EditText connectBandwidthAmountText = (EditText) dialogView.findViewById(R.id.bandwidth_amount);
        builder.setView(dialogView);
        builder.setTitle("Parameters");
        builder.setPositiveButton("Scan", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(isNumeric(connectDurationText.getText().toString()) && isNumeric(connectBandwidthAmountText.getText().toString())) {
                    Intent i = new Intent(getApplicationContext(), ConnectHotspotActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("bandwidthDuration", connectDurationText.getText().toString());
                    bundle.putString("bandwidthAmount", connectBandwidthAmountText.getText().toString());
                    i.putExtra("data", bundle);
                    startActivity(i);
                } else {
                    Toast.makeText(getApplicationContext(), "Duration and bandwidth amount must be integers. Please enter your settings again.", Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        AlertDialog dialog=builder.create();
        dialog.show();
    }


    public boolean isNumeric(String str){
        for (int i = 0; i < str.length(); i++){
            if (!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
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
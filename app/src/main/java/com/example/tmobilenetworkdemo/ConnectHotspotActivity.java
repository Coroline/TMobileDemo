package com.example.tmobilenetworkdemo;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.tmobilenetworkdemo.Wifi.NetworkStatsHelper;
import com.example.tmobilenetworkdemo.Wifi.PackageManagerHelper;
import com.example.tmobilenetworkdemo.Wifi.TrafficStatsHelper;
import com.example.tmobilenetworkdemo.Wifi.WifiAdmin;

import java.util.List;

public class ConnectHotspotActivity extends AppCompatActivity {

    private Button scan;
    private WifiAdmin mWifiAdmin;
    private TextView networkStatsAllRx;
    private TextView networkStatsAllTx;
    private TextView networkStatsPackageRx;
    private TextView networkStatsPackageTx;
    private TextView trafficStatsAllRx;
    private TextView trafficStatsAllTx;
    private TextView trafficStatsPackageRx;
    private TextView trafficStatsPackageTx;

    private static final int READ_PHONE_STATE_REQUEST = 37;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_hotspot);
        scan = findViewById(R.id.scan);
        networkStatsAllRx = findViewById(R.id.networkStatsAllRx);
        networkStatsAllTx = findViewById(R.id.networkStatsAllTx);
        networkStatsPackageRx = findViewById(R.id.networkStatsPackageRx);
        networkStatsPackageTx = findViewById(R.id.networkStatsPackageTx);
        trafficStatsAllRx = findViewById(R.id.trafficStatsAllRx);
        trafficStatsAllTx = findViewById(R.id.trafficStatsAllTx);
        trafficStatsPackageRx = findViewById(R.id.trafficStatsPackageRx);
        trafficStatsPackageTx = findViewById(R.id.trafficStatsPackageTx);

        mWifiAdmin = new WifiAdmin(this);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<ScanResult> scanResult = mWifiAdmin.getScanResultList();
                System.out.println(scanResult);
            }
        });

        fillData(getPackageName());
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onStart() {
        super.onStart();
        requestPermissions();
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void requestPermissions() {
        if (!hasPermissionToReadNetworkHistory()) {
            return;
        }
        if (!hasPermissionToReadPhoneStats()) {
            requestPhoneStateStats();
        }
    }


    private boolean hasPermissionToReadPhoneStats() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
            return false;
        } else {
            return true;
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private boolean hasPermissionToReadNetworkHistory() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        final AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        if (mode == AppOpsManager.MODE_ALLOWED) {
            return true;
        }
        appOps.startWatchingMode(AppOpsManager.OPSTR_GET_USAGE_STATS,
                getApplicationContext().getPackageName(),
                new AppOpsManager.OnOpChangedListener() {
                    @Override
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onOpChanged(String op, String packageName) {
                        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                                android.os.Process.myUid(), getPackageName());
                        if (mode != AppOpsManager.MODE_ALLOWED) {
                            return;
                        }
                        appOps.stopWatchingMode(this);
                        Intent intent = new Intent(ConnectHotspotActivity.this, ConnectHotspotActivity.class);
                        if (getIntent().getExtras() != null) {
                            intent.putExtras(getIntent().getExtras());
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplicationContext().startActivity(intent);
                    }
                });
        requestReadNetworkHistoryAccess();
        return false;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void requestReadNetworkHistoryAccess() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    }


    private void requestPhoneStateStats() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, READ_PHONE_STATE_REQUEST);
    }



    private void fillData(String packageName) {
        int uid = PackageManagerHelper.getPackageUid(this, packageName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkStatsManager networkStatsManager = (NetworkStatsManager) getApplicationContext().getSystemService(Context.NETWORK_STATS_SERVICE);
            NetworkStatsHelper networkStatsHelper = new NetworkStatsHelper(networkStatsManager, uid);
            fillNetworkStatsAll(networkStatsHelper);
            fillNetworkStatsPackage(uid, networkStatsHelper);
        }
        fillTrafficStatsAll();
        fillTrafficStatsPackage(uid);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void fillNetworkStatsAll(NetworkStatsHelper networkStatsHelper) {
        long mobileWifiRx = networkStatsHelper.getAllRxBytesMobile(this) + networkStatsHelper.getAllRxBytesWifi();
        networkStatsAllRx.setText(mobileWifiRx + " B");
        long mobileWifiTx = networkStatsHelper.getAllTxBytesMobile(this) + networkStatsHelper.getAllTxBytesWifi();
        networkStatsAllTx.setText(mobileWifiTx + " B");
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void fillNetworkStatsPackage(int uid, NetworkStatsHelper networkStatsHelper) {
        long mobileWifiRx = networkStatsHelper.getPackageRxBytesMobile(this) + networkStatsHelper.getPackageRxBytesWifi();
        networkStatsPackageRx.setText(mobileWifiRx + " B");
        networkStatsPackageRx.setTextColor(Color.parseColor("#FFFFFF"));
        long mobileWifiTx = networkStatsHelper.getPackageTxBytesMobile(this) + networkStatsHelper.getPackageTxBytesWifi();
        networkStatsPackageTx.setText(mobileWifiTx + " B");
        networkStatsPackageTx.setTextColor(Color.parseColor("#FFFFFF"));
    }

    private void fillTrafficStatsAll() {
        trafficStatsAllRx.setText(TrafficStatsHelper.getAllRxBytes() + " B");
        trafficStatsAllTx.setText(TrafficStatsHelper.getAllTxBytes() + " B");
        trafficStatsAllRx.setTextColor(Color.parseColor("#FFFFFF"));
        trafficStatsAllTx.setTextColor(Color.parseColor("#FFFFFF"));
    }

    private void fillTrafficStatsPackage(int uid) {
        trafficStatsPackageRx.setText(TrafficStatsHelper.getPackageRxBytes(uid) + " B");
        trafficStatsPackageTx.setText(TrafficStatsHelper.getPackageTxBytes(uid) + " B");
        trafficStatsPackageRx.setTextColor(Color.parseColor("#FFFFFF"));
        trafficStatsPackageTx.setTextColor(Color.parseColor("#FFFFFF"));
    }
}

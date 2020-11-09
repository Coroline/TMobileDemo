package com.example.tmobilenetworkdemo;

import android.annotation.TargetApi;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tmobilenetworkdemo.Model.ConnectDevice;
import com.example.tmobilenetworkdemo.Wifi.NetworkStatsHelper;
import com.example.tmobilenetworkdemo.Wifi.PackageManagerHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CreatedHotspotInformationActivity extends AppCompatActivity implements RecyclerViewAdapterConnectedUser.onDeviceSelectedListener {

    private TextView hotspotName;
    private TextView totalSharingData;
    private long startTime = System.currentTimeMillis();
    private long endTime = System.currentTimeMillis();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_created_hotspot_info);
        hotspotName = findViewById(R.id.hotspot_name);
        totalSharingData = findViewById(R.id.total_sharing_data);

        Intent intent = getIntent();
        Bundle bundle=intent.getBundleExtra("data");
        String nameSSID = bundle.getString("hotspotName");
        hotspotName.setText(nameSSID);

        fillData(getPackageName());

        // Dummy data
        List<ConnectDevice> l = new ArrayList<>();
        l.add(new ConnectDevice("10.2.8.19", "01:34:EF:5D:18:4C"));
        l.add(new ConnectDevice("10.2.8.30", "01:34:EF:7B:22:CD"));
        initRecyclerView(l);
    }


    // Recycler View
    private void initRecyclerView(List<ConnectDevice> connectDeviceList) {
        RecyclerView recyclerView = findViewById(R.id.connected_user_list);
        RecyclerViewAdapterConnectedUser adapter = new RecyclerViewAdapterConnectedUser(connectDeviceList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onDeviceSelected(ConnectDevice selectedDevice) {
        // TODO: Pop up a window to show device and connected user's detail information
    }


    private void fillData(String packageName) {
        Timer timer = new Timer();
        timer.schedule(new MyTask(), 0, 5000);  //任务等待0秒后开始执行，之后每秒执行一次
    }


    class MyTask extends TimerTask {
        @Override
        public void run() {
            int uid = PackageManagerHelper.getPackageUid(getApplicationContext(), getPackageName());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                NetworkStatsManager networkStatsManager = (NetworkStatsManager) getApplicationContext().getSystemService(Context.NETWORK_STATS_SERVICE);
                NetworkStatsHelper networkStatsHelper = new NetworkStatsHelper(networkStatsManager, uid);

                Date day = new Date();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                endTime = day.getTime();

                long mobileWifiRx = networkStatsHelper.getAllRxBytesMobile(getApplicationContext(), startTime, endTime) + networkStatsHelper.getAllRxBytesWifi();
                long mobileWifiTx = networkStatsHelper.getAllTxBytesMobile(getApplicationContext(), startTime, endTime) + networkStatsHelper.getAllTxBytesWifi();
                System.out.println(df.format(day) + " " + day.getTime() + " " + startTime + " " + endTime + " " + "\n");
                System.out.println(mobileWifiRx + mobileWifiTx);
                startTime = day.getTime();
            }
        }
    }
}

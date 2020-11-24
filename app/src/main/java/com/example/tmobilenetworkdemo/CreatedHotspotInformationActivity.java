package com.example.tmobilenetworkdemo;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tmobilenetworkdemo.Lib.NetworkInformationManager;
import com.example.tmobilenetworkdemo.Lib.UserInformationManager;
import com.example.tmobilenetworkdemo.Model.ConnectedUserInfo;

import org.json.JSONException;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CreatedHotspotInformationActivity extends AppCompatActivity implements RecyclerViewAdapterConnectedUser.onConnUserSelectedListener {
    private static long queryDelay = 5000;
    private static long queryInterval = 5000;

    private TextView hotspotName;
    private TextView totalSharingData;
    private NetworkInformationManager manager;
    private Timer queryUsageTimer;
    private static final String TAG = "CreatedHotspotInformationActivity";

    private class QueryTimerTask extends TimerTask {
        @Override
        public void run() {
            try {
                manager.queryBandwidthUsage(UserInformationManager.token, new NetworkInformationManager.OnBandwidthQueryListener() {
                    @Override
                    public void onSuccess(List<ConnectedUserInfo> list) {
                        Log.d(TAG, "Client find connected user successfully.");
                        int totalUsage = 0;
                        for (ConnectedUserInfo e : list) {
                            totalUsage += e.getBandwidthUsage();
                        }
                        totalSharingData.setText(totalUsage + " MB");
                        initRecyclerView(list);
                    }

                    @Override
                    public void onNetworkFail() {

                    }

                    @Override
                    public void onFail() {

                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_created_hotspot_info);
        hotspotName = findViewById(R.id.hotspot_name);
        totalSharingData = findViewById(R.id.total_sharing_data);
        manager = NetworkInformationManager.getInstance(getApplicationContext());
        queryUsageTimer = new Timer();

        Intent intent = getIntent();
        Bundle bundle=intent.getBundleExtra("data");
        String nameSSID = bundle.getString("hotspotName");
        hotspotName.setText(nameSSID);

        startQueryUsage(queryDelay, queryInterval);

//        List<ConnectedUserInfo> l = new ArrayList<>();
//        int totalUsage = 0;
//        for(ConnectedUserInfo e: l) {
//            totalUsage += e.getBandwidthUsage();
//        }
//        totalSharingData.setText(totalUsage + " MB");
//        initRecyclerView(l);
    }


    // Recycler View
    private void initRecyclerView(List<ConnectedUserInfo> connectDeviceList) {
        RecyclerView recyclerView = findViewById(R.id.connected_user_list);
        RecyclerViewAdapterConnectedUser adapter = new RecyclerViewAdapterConnectedUser(connectDeviceList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void startQueryUsage(long delay, long interval) {
        queryUsageTimer.schedule(new QueryTimerTask(), delay, interval);
    }

    private void stopQueryUsage() {
        queryUsageTimer.cancel();
    }

    @Override
    public void onConnUserSelected(ConnectedUserInfo selectedConnUser) {
        LayoutInflater factory = LayoutInflater.from(this);
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        final View dialogView = factory.inflate(R.layout.dialog_connected_user_detail, null);
        builder.setView(dialogView);
        builder.setTitle(selectedConnUser.getUsername());
        TextView curBandwidthUsage = dialogView.findViewById(R.id.user_bandwidth_amount);
        TextView curBandwidthDuration = dialogView.findViewById(R.id.user_bandwidth_duration);
        curBandwidthUsage.setText(selectedConnUser.getBandwidthUsage());
        curBandwidthDuration.setText(selectedConnUser.getDuration());
        builder.setPositiveButton("OK", null);
        AlertDialog dialog=builder.create();
        dialog.show();
    }
}

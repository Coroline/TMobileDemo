package com.example.tmobilenetworkdemo;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tmobilenetworkdemo.Lib.GPSTracking;
import com.example.tmobilenetworkdemo.Lib.NetworkInformationManager;
import com.example.tmobilenetworkdemo.Lib.UserInformationManager;
import com.example.tmobilenetworkdemo.Model.ConnectedUserInfo;

import org.json.JSONException;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Activity for a client after he create a hotspot, show information including:
 * Hotspot SSID, total bandwidth usage and credit usage, connected users list
 */
public class CreatedHotspotInformationActivity extends AppCompatActivity implements RecyclerViewAdapterConnectedUser.onConnUserSelectedListener {
    private static long queryDelay = 5000;
    private static long queryInterval = 5000;

    private TextView hotspotName;
    private TextView backButtonText;
    private TextView totalSharingData;
    private TextView totalCreditData;
    private Button stopSharingBtn;
    private List<ConnectedUserInfo> connUserList = new ArrayList<>();
    private NetworkInformationManager manager;
    private Timer queryUsageTimer;
    private GPSTracking locationService;
    private ServiceConnection conn;
    private Timer updateLocationTimer;
    private static final String TAG = "CreatedHotspotInformationActivity";


    /**
     * Timer to get latest bandwidth and credit usage every 5 seconds
     */
    private class QueryTimerTask extends TimerTask {
        @Override
        public void run() {
            try {
                manager.queryBandwidthUsage(UserInformationManager.token, new NetworkInformationManager.OnBandwidthQueryListener() {
                    @Override
                    public void onSuccess(List<ConnectedUserInfo> list, double clientCredit) {
                        Log.d(TAG, "Client find connected user successfully.");
                        connUserList.clear();
                        connUserList.addAll(list);
                        double totalUsage = 0;
                        for (ConnectedUserInfo e : list) {
                            totalUsage += e.getBandwidthUsage();
                        }
                        DecimalFormat df = new DecimalFormat("#.00");
                        String usageString = df.format(totalUsage);
                        if(totalUsage < 1.0) {
                            usageString = "0" + usageString;
                        }
                        String creditString = df.format(clientCredit);
                        if(clientCredit < 1.0) {
                            creditString = "0" + creditString;
                        }

                        totalSharingData.setText(usageString + " MB");
                        totalCreditData.setText("+" + creditString + " credit");
                        initRecyclerView(connUserList);
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
        backButtonText = findViewById(R.id.backButtonText);
        stopSharingBtn = findViewById(R.id.stopSharingBtn);
        totalCreditData = findViewById(R.id.total_credit_data);
        manager = NetworkInformationManager.getInstance(getApplicationContext());
        queryUsageTimer = new Timer();

        backButtonText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back(view);
            }
        });

        Intent intent = getIntent();
        Bundle bundle=intent.getBundleExtra("data");
        String nameSSID = bundle.getString("hotspotName");
        hotspotName.setText(nameSSID);

        stopSharingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    clientStopSharing();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        // Location update service
        Intent serviceIntent = new Intent(this, GPSTracking.class);
        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                GPSTracking.MyBinder binder = (GPSTracking.MyBinder) service;
                locationService = binder.getService();
                Location loc = locationService.getLocation();
                System.out.println(loc.getLatitude() + " | " + loc.getLongitude());
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                locationService = null;
            }
        };
        getApplicationContext().bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);
        updateLocationTimer = new Timer();
        updateLocationTimer.schedule(new MyTask(), 0, 5000);
        startQueryUsage(queryDelay, queryInterval);
    }


    /**
     * Timer to update client's location every 5 seconds
     */
    class MyTask extends TimerTask {
        @Override
        public void run() {
            if(GPSTracking.lat != 0.0 && GPSTracking.lng != 0.0) {
                try {
                    manager.updateClientLocation(UserInformationManager.token, GPSTracking.lat, GPSTracking.lng, new NetworkInformationManager.OnClientLocationUpdateListener() {
                        @Override
                        public void onSuccess(String result) {
                            if (result.equals("true")) {
                                System.out.println("Location updated " + GPSTracking.lat + " " + GPSTracking.lng);
                            } else {
                                Log.d(TAG, "Failed to update location");
                            }
                        }

                        @Override
                        public void onNetworkFail() {
                            Log.d(TAG, "Update location network failure.");
                        }

                        @Override
                        public void onFail() {
                            Log.d(TAG, "Update location failure.");
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // Recycler View
    private void initRecyclerView(List<ConnectedUserInfo> connectDeviceList) {
        RecyclerView recyclerView = findViewById(R.id.connected_user_list);
        RecyclerViewAdapterConnectedUser adapter = new RecyclerViewAdapterConnectedUser(connectDeviceList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Start query bandwidth and credit usage timer
     * @param delay
     * @param interval
     */
    private void startQueryUsage(long delay, long interval) {
        queryUsageTimer.schedule(new QueryTimerTask(), delay, interval);
    }


    private void stopQueryUsage() {
        queryUsageTimer.cancel();
    }


    /**
     * Show a dialog after the client clicked a connected user item
     * @param selectedConnUser Selected user item
     */
    @Override
    public void onConnUserSelected(final ConnectedUserInfo selectedConnUser) {
        LayoutInflater factory = LayoutInflater.from(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View dialogView = factory.inflate(R.layout.dialog_connected_user_detail, null);
        builder.setView(dialogView);
        builder.setTitle(selectedConnUser.getUsername());
        TextView curBandwidthUsage = dialogView.findViewById(R.id.user_bandwidth_amount);
        TextView curBandwidthDuration = dialogView.findViewById(R.id.user_bandwidth_duration);
        Button disconnectUser = dialogView.findViewById(R.id.disconnectUser);
        curBandwidthUsage.setText(String.valueOf(selectedConnUser.getBandwidthUsage()));
        curBandwidthDuration.setText(String.valueOf(selectedConnUser.getDuration()));
        disconnectUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    disconnectUser(selectedConnUser);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setPositiveButton("OK", null);
        AlertDialog dialog=builder.create();
        dialog.show();
    }


    /**
     * Client stops sharing bandwidth
     * @throws JSONException
     */
    public void clientStopSharing() throws JSONException {
        manager.stopSharing(UserInformationManager.token, new NetworkInformationManager.OnStopSharingListener() {
            @Override
            public void onSuccess(Boolean status) {
                if(status) {
                    totalSharingData.setText("0 MB");
                    totalCreditData.setText("+0 credit");
                    initRecyclerView(connUserList);
                    Log.d(TAG, "You have successfully stopped sharing bandwidth.");
                    queryUsageTimer.cancel();
                    updateLocationTimer.cancel();
                } else {
                    Log.d(TAG, "Client stops sharing failed");
                }
            }

            @Override
            public void onNetworkFail() {
                Log.d(TAG, "Client stop sharing encountered network failure.");
            }

            @Override
            public void onFail() {
                Log.d(TAG, "Client stop sharing encountered failure.");
            }
        });
    }


    /**
     * Disconnect selected user
     * @param connectedUserInfo Class to store connected user's information with this hotspot
     * @throws JSONException
     */
    public void disconnectUser(ConnectedUserInfo connectedUserInfo) throws JSONException {
        manager.disconnectUser(UserInformationManager.token, connectedUserInfo.getConnectionId(), new NetworkInformationManager.OnDisconnectUserListener() {
            @Override
            public void onSuccess(Boolean status) {
                if(status) {
                    Log.d(TAG, "Client disconnection is successful.");
                } else {
                    Log.d(TAG, "Client disconnect this user failed");
                }
            }

            @Override
            public void onNetworkFail() {
                Log.d(TAG, "Client disconnect this user encountered network failure.");
            }

            @Override
            public void onFail() {
                Log.d(TAG, "Client disconnect this user encountered failure.");
            }
        });
    }


    /**
     * Use Android's stack to take user to the previous screen.
     */
    public void back(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

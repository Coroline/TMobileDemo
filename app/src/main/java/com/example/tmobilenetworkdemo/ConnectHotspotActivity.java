package com.example.tmobilenetworkdemo;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.usage.NetworkStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.TrafficStats;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tmobilenetworkdemo.Lib.GPSTracking;
import com.example.tmobilenetworkdemo.Lib.NetworkInformationManager;
import com.example.tmobilenetworkdemo.Lib.UserInformationManager;
import com.example.tmobilenetworkdemo.Model.ConnectedUserInfo;
import com.example.tmobilenetworkdemo.Wifi.NetworkStatsHelper;
import com.example.tmobilenetworkdemo.Wifi.PackageManagerHelper;
import com.example.tmobilenetworkdemo.Wifi.WifiAdmin;

import org.json.JSONException;
import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class ConnectHotspotActivity extends AppCompatActivity implements RecyclerViewAdapterNearbyWifi.onWifiSelectedListener {

    public static boolean active;

    private Button scan;
    private WifiAdmin mWifiAdmin;
    private TextView currentConnection;
    private Button chargeStandard;
    private RecyclerView wifi_recyclerView;
    private TextView backButtonText;
    private TextView currentCreditUsage;
    private ImageView imageView2;
    List<ScanResult> scanResult = new ArrayList<>();
    List<ScanResult> nearbyClient = new ArrayList<>();
    private String currentSSID;
    public static boolean mIsConnectingWifi=false;
    public static boolean mIsFirstReceiveConnected=false;
    private ServiceConnection conn;
    private GPSTracking locationService;

    private static final int READ_PHONE_STATE_REQUEST = 37;
    private long connectionStartTime = 0;
    private long startTime = System.currentTimeMillis();
    private long endTime = System.currentTimeMillis();

    double totalWifi;
    public static String wifiTraffic;
    public static double wf = 0;
    public static double lastWf = 0;
    private TextView currentBandwidthUsage;
    private WifiManager wifiManager;
    public static int wifiStr;
    DecimalFormat df = new DecimalFormat(".##");
    private TextView bandwidthUsageHead;
    private Handler handler1= null;
    int hours = 0,minutes = 0,seconds = 0;
    private NetworkInformationManager networkInformationManager;
    private int connectDuration;    // Parameters for searching clients
    private int connectionAmount;
    private Thread timerThread = null;
    private Thread updateThread = null;
    private String mSSID = "";
    private int networkID = -1;
    private Timer queryBandwidthTimer = null;

    private static final String TAG = "ConnectHotspotActivity";

    private class QueryTimerTask extends TimerTask {
        @Override
        public void run() {
            try {
                networkInformationManager.queryConnectionBandwidthUsage(UserInformationManager.token, UserInformationManager.connectionId, new NetworkInformationManager.OnConnectionBandwidthUsageListener() {
                    @Override
                    public void onSuccess(double bandwidthUsed, int duration, double creditsTransferred) {
                        Log.i("bandwidthused", String.format("%.2f", bandwidthUsed));
                        currentBandwidthUsage.setText(String.format("%.2f", bandwidthUsed) + " MB");
                        currentCreditUsage.setText(String.format("%.2f", creditsTransferred) + " credit");
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
        setContentView(R.layout.activity_connect_hotspot);
        backButtonText = findViewById(R.id.backButtonText);
        currentConnection = findViewById(R.id.current_internet);
        wifi_recyclerView = findViewById(R.id.wifi_recyclerView);
        imageView2 = findViewById(R.id.imageView2);
        wifi_recyclerView.setVisibility(View.INVISIBLE);
        imageView2.setVisibility(View.VISIBLE);
        currentBandwidthUsage = findViewById(R.id.current_bandwidth_usage);
        bandwidthUsageHead = findViewById(R.id.bandwidth_usage_head);
        chargeStandard = findViewById(R.id.chargeStandard);
        currentCreditUsage = findViewById(R.id.current_credit_usage);
        networkInformationManager = NetworkInformationManager.getInstance(getApplicationContext());

        active = true;

        backButtonText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                // forget network
//                active = false;
//                wifiManager.removeNetwork(networkID);
//                wifiManager.saveConfiguration();
//
//                updateThread.interrupt();
//                timerThread.interrupt();
                back(view);
            }
        });

        Intent intent = getIntent();
        Bundle bundle=intent.getBundleExtra("data");
        connectDuration = Integer.parseInt(Objects.requireNonNull(bundle.getString("bandwidthDuration")));
        connectionAmount = Integer.parseInt(Objects.requireNonNull(bundle.getString("bandwidthAmount")));

        mWifiAdmin = new WifiAdmin(this);
        currentSSID = mWifiAdmin.getSSID();
        if(currentSSID.equals("NULL"))
            currentConnection.setText("No Internet Connection");
        else
            currentConnection.setText(currentSSID);
        chargeStandard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChargeRate();
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
//                System.out.println(loc.getLatitude() + " | " + loc.getLongitude());
                try {
                    //todo: change to real location
                    networkInformationManager.findClients(UserInformationManager.token, 0.0, 0.0, connectionAmount, connectDuration, new NetworkInformationManager.OnFindClientsListener() {
                        @Override
                        public void onSuccess(HashMap<String, String> result) {
                            System.out.println("find client result: " + result);
                            scanResult = mWifiAdmin.getScanResultList();
                            for(Map.Entry<String, String> entry: result.entrySet()) {    // Get intersection of scanned result and backend result
                                for(int i = 0; i < scanResult.size(); i++) {
                                    if(entry.getKey().equals(scanResult.get(i).SSID))
                                        nearbyClient.add(scanResult.get(i));
                                }
                            }
                            System.out.println(scanResult);    // Original Wifi List
                            System.out.println(nearbyClient);  // List of hotspot that is created via this app and meet bandwidth and interval parameter settings
                            if(nearbyClient.size() == 0) {     // No corresponding clients to meet parameter requirements, pop up a dialog
                                showNoClientDialog();
                            } else {
                                initRecyclerView(nearbyClient);
                                wifi_recyclerView.setVisibility(View.VISIBLE);
                                imageView2.setVisibility(View.INVISIBLE);
                            }
                        }

                        @Override
                        public void onNetworkFail() {
                            System.out.println("find client failed - network.");
                        }

                        @Override
                        public void onFail() {
                            System.out.println("find client failed.");
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                locationService = null;
            }
        };
        getApplicationContext().bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);
        // result info:
        // SSID: DoNotConnectMe_5GEXT, BSSID: cc:40:d0:f0:af:38, capabilities: [WPA-PSK-CCMP+TKIP][WPA2-PSK-CCMP+TKIP][WPS][ESS], level: -60, frequency: 5765, timestamp: 524626056217, distance: ?(cm), distanceSd: ?(cm), passpoint: no, ChannelBandwidth: 2, centerFreq0: 5775, centerFreq1: 0, 80211mcResponder: is not supported,

    }


    private void newHotspotConnectionClicked() {
        seconds = 0;
        wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiStr = wifiManager.getWifiState();

        if(wifiStr != 3){
            Toast.makeText(this, "Disconnect from WiFi", Toast.LENGTH_LONG).show();
        }
        if(wifiStr == 3){
            final Handler handler = new Handler(){
                public void handleMessage(Message msg){
                    super.handleMessage(msg);
                    if(msg.what == 1){
                        try {
                            final double currentWf = Double.parseDouble(wifiTraffic);
                            networkInformationManager.updateBandwidthUsage(UserInformationManager.token, UserInformationManager.connectionId, currentWf-lastWf, new NetworkInformationManager.OnBandwidthUpdateListener() {
                                @Override
                                public void onSuccess(String result) {
                                    System.out.println(result);
                                    if (result.equals("false")) {
                                        lastWf = currentWf;
                                    } else {
                                        //should disconnect
                                        Log.d(TAG, "should disconnect!");

                                        if (active) {
                                            // forget network
                                            active = false;
                                            queryBandwidthTimer.cancel();
                                            wifiManager.removeNetwork(networkID);
                                            wifiManager.saveConfiguration();
                                            updateThread.interrupt();
                                            timerThread.interrupt();
                                            onBackPressed();
                                        }
                                    }
                                }

                                @Override
                                public void onNetworkFail() {
                                    Toast.makeText(getApplicationContext(), "Disconnect from WiFi", Toast.LENGTH_LONG).show();
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
            };

            if(wifiStr == 3){
                handler1 = new Handler(){
                    public void handleMessage(Message msg1){
                        super.handleMessage(msg1);
                        if(msg1.what == 1){
                            bandwidthUsageHead.setText(new DecimalFormat("00").format(hours) + ":" +
                                    new DecimalFormat("00").format(minutes) + ":" + new DecimalFormat("00").format(seconds));
                        }
                    }
                };
            }

            timerThread = new Thread(new Runnable() {
                public void run(){
                    for(int i = 0; ; i++){
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
// TODO Auto-generated catch block
                            System.out.println("need close thread!");
                            break;
                        }
                        seconds += 5;
                        Message msg1 = new Message();
                        msg1.what = 1;
                        handler1.sendMessage(msg1);
                        if(seconds == 60){
                            seconds = 0;
                            minutes++;
                            if(minutes == 60){
                                minutes = 0;
                                hours++;
                            }
                        }
                    }
                }
            });
            timerThread.start();

            updateThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true){
                        double currentTime = System.currentTimeMillis();
//  getWifiTraffic(currentTime);
                        double totalWifi01 = getWifiTraffic(currentTime);;
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
// TODO Auto-generated catch block
                            System.out.println("need close thread!");
                            break;
                        }
                        double frontTime = System.currentTimeMillis();
//  getWifiTraffic(frontTime);
                        double totalWifi02 = getWifiTraffic(frontTime);
                        double errorTraffic = totalWifi02 - totalWifi01;
                        if(errorTraffic < 512){
                            errorTraffic = 1;
                        }
//                        wf += errorTraffic/1111500;
                        wf += errorTraffic/1050000;
//                        wf += errorTraffic;
                        wifiTraffic = df.format(wf);
//  Log.i("使用的流量", wifiTraffic + "");
                        Message message = new Message();
                        message.what = 1;
                        handler.sendMessage(message);
                    }
                }
            });
            updateThread.start();

            queryBandwidthTimer = new Timer();
            queryBandwidthTimer.schedule(new QueryTimerTask(), 0, 5000);
        }
    }


    public void showNoClientDialog(){
        LayoutInflater factory = LayoutInflater.from(this);
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        final View dialogView = factory.inflate(R.layout.fragment_no_client_dialog, null);
        builder.setView(dialogView);
        builder.setTitle("Oops!");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
            }
        });
        AlertDialog dialog=builder.create();
        dialog.show();
    }


    public double getWifiTraffic(double time){
        double rtotalGprs = TrafficStats.getTotalRxBytes();
        double ttotalGprs = TrafficStats.getTotalTxBytes();
        double rgprs = TrafficStats.getMobileRxBytes();
        double tgprs = TrafficStats.getMobileTxBytes();
        double rwifi = rtotalGprs - rgprs;
        double twifi = ttotalGprs - tgprs;
        totalWifi = rwifi + twifi;
        return totalWifi;
        //totalWifi = rtotalGprs + ttotalGprs;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onStart() {
        super.onStart();
        requestPermissions();
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void requestPermissions() {
//        if (!hasPermissionToReadNetworkHistory()) {
//            return;
//        }
//        if (!hasPermissionToReadPhoneStats()) {
//            requestPhoneStateStats();
//        }
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


    // Recycler View
    private void initRecyclerView(List<ScanResult> wifiDetailList) {
        RecyclerView recyclerView = findViewById(R.id.wifi_recyclerView);
        RecyclerViewAdapterNearbyWifi adapter = new RecyclerViewAdapterNearbyWifi(wifiDetailList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void changeGui(String password) {
        Log.d(TAG, "password is: " + password);
    }

    @Override
    public void onWifiSelected(final ScanResult selectedWifi) {
        boolean isLocked = selectedWifi.capabilities.contains("WEP") || selectedWifi.capabilities.contains("PSK");
        if(!selectedWifi.SSID.equals(mWifiAdmin.getSSID()) && !selectedWifi.BSSID.equals(mWifiAdmin.getBSSID())) {
            Log.d(TAG, "onItemClick: Selected wifi is not current connected wifi.");
            WifiConfiguration configuration = mWifiAdmin.isExsits(selectedWifi.SSID);
            if(configuration == null) {
                Log.d(TAG, "oClick: wifi has not been configured.");
                if(isLocked) {
                    try {
                        networkInformationManager.requestConnection(UserInformationManager.token, NetworkInformationManager.ssidIdMap.get(selectedWifi.SSID), connectionAmount, connectDuration, new NetworkInformationManager.OnRequestConnectionListener() {
                            @Override
                            public void onSuccess(String password, int connectionId) {
                                Log.d(TAG, "password is: " + password);
                                UserInformationManager.connectionId = connectionId;

                                if (selectedWifi.capabilities.contains("WEP")) {
                                    networkID = connecting(selectedWifi, password, 2);
                                } else {
                                    networkID = connecting(selectedWifi, password, 3);
                                }

                                mSSID = selectedWifi.SSID;
                                newHotspotConnectionClicked();
                            }

                            @Override
                            public void onFail() {
                                showEditPwdDialog(selectedWifi);
                            }

                            @Override
                            public void onNetworkFail() {
                                showEditPwdDialog(selectedWifi);
                            }
                        });
                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "no password.");
                    connecting(selectedWifi, null, 1);
                }
            } else {
                Log.d(TAG, "wifi has been configured.");
                ConnectHotspotActivity.mIsConnectingWifi = true;
                ConnectHotspotActivity.mIsFirstReceiveConnected = true;
                // wifiConnecting
                if(mWifiAdmin.connectConfiguration(configuration)) {
                    currentConnection.setText(selectedWifi.SSID);
                    System.out.println("Connection Success.");
                    connectionStartTime = System.currentTimeMillis();
                } else {
                    System.out.println("Connection Error.");
                }
            }
        }
        System.out.println("connectionStartTime value: " + connectionStartTime);
    }


    private void showEditPwdDialog(final ScanResult selectedWifi) {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_edit_wifi_pwd,null);
        final EditText editText = v.findViewById(R.id.et_password);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog = builder.
                setTitle(selectedWifi.SSID).
                setView(v).
                setNegativeButton(android.R.string.cancel,null)
                .setPositiveButton("connect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String psd=editText.getText().toString();
                        if (!psd.isEmpty()) {
                            if (selectedWifi.capabilities.contains("WEP")) {
                                connecting(selectedWifi, psd,2);
                            }else {
                                connecting(selectedWifi, psd,3);
                            }
                        }
                    }
                }).create();

        dialog.show();

        final Button positiveTv = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveTv.setEnabled(false);
        positiveTv.setTextColor(getResources().getColor(R.color.accent_grey));
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length()<8) {
                    positiveTv.setTextColor(getResources().getColor(R.color.accent_grey));
                    positiveTv.setEnabled(false);
                }else {
                    positiveTv.setTextColor(getResources().getColor(R.color.colorAccent));
                    positiveTv.setEnabled(true);
                }
            }
        });
    }


    /**
     * connect wifi
     * @param result selected wifi
     * @param pass wifi password
     * @param type wifi encryption type：0-no password，1-WEP，2-WPA
     */
    private int connecting(ScanResult result, String pass, int type){
        if (BuildConfig.DEBUG)
            Log.d(TAG, "connecting: ***********************************************************************");
        mIsConnectingWifi = true;
        mIsFirstReceiveConnected = true;
        int wcgID = mWifiAdmin.connectWifi(result.SSID, pass, type);
        Log.d(TAG, "connecting: " + wcgID);
        if (wcgID == -1) {
            System.out.println("Connection Error.");
        } else {
            currentConnection.setText(result.SSID);
            connectionStartTime = System.currentTimeMillis();
        }
        return wcgID;
    }

    // Animation Effects
//    public void wifiConnecting(String ssid){
//        llconnected.setVisibility(View.GONE);
//        mIvShare.setVisibility(View.GONE);
//
//        mCvSate.setVisibility(View.VISIBLE);
//        mRippleBackground.setVisibility(View.VISIBLE);
//        mTvName.setText(ssid);
//        mRippleBackground.startRippleAnimation();
//        AnimationDrawable ad= (AnimationDrawable) mIvCenter.getDrawable();
//        ad.start();
//    }


    public void showChargeRate() {
        LayoutInflater factory = LayoutInflater.from(this);
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        final View dialogView = factory.inflate(R.layout.dialog_charge_rate, null);
        builder.setView(dialogView);
        builder.setTitle("Charge Rate");
        builder.setPositiveButton("OK", null);
        AlertDialog dialog=builder.create();
        dialog.show();
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
package com.example.tmobilenetworkdemo;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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

    private Button scan;
    private WifiAdmin mWifiAdmin;
    private TextView currentConnection;
    private RecyclerView wifi_recyclerView;
    private ImageView imageView2;
    List<ScanResult> scanResult = new ArrayList<>();
    List<ScanResult> nearbyClient = new ArrayList<>();
    private String currentSSID;
    public static boolean mIsConnectingWifi=false;
    public static boolean mIsFirstReceiveConnected=false;

    private static final int READ_PHONE_STATE_REQUEST = 37;
    private long connectionStartTime = 0;
    private long startTime = System.currentTimeMillis();
    private long endTime = System.currentTimeMillis();

    double totalWifi;
    public static String wifiTraffic ;
    public static double wf = 0;
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

    private static final String TAG = "ConnectHotspotActivity";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_hotspot);
//        scan = findViewById(R.id.scan);
        currentConnection = findViewById(R.id.current_internet);
        wifi_recyclerView = findViewById(R.id.wifi_recyclerView);
        imageView2 = findViewById(R.id.imageView2);
        wifi_recyclerView.setVisibility(View.INVISIBLE);
        imageView2.setVisibility(View.VISIBLE);
        currentBandwidthUsage = findViewById(R.id.current_bandwidth_usage);
        bandwidthUsageHead = findViewById(R.id.bandwidth_usage_head);
        networkInformationManager = NetworkInformationManager.getInstance(getApplicationContext());

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

//        scanResult = mWifiAdmin.getScanResultList();
//        System.out.println(scanResult);
//        initRecyclerView(scanResult);
//        wifi_recyclerView.setVisibility(View.VISIBLE);
//        imageView2.setVisibility(View.INVISIBLE);

        try {
            networkInformationManager.findClients(UserInformationManager.token, GPSTracking.lat, GPSTracking.lng, connectionAmount, connectDuration, new NetworkInformationManager.OnFindClientsListener() {
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
                        if(Double.parseDouble(wifiTraffic) < 1){
                            Log.i("wifiTraffic", wifiTraffic);
                            currentBandwidthUsage.setText("0" + wifiTraffic + "MB");
                        }
                        else{
                            currentBandwidthUsage.setText(wifiTraffic + "MB");
                        }
                        System.out.println("||||||||||||||||||||||||||||||||");
                        try {
                            System.out.println("?????????????????????????");
                            networkInformationManager.updateBandwidthUsage(UserInformationManager.token, UserInformationManager.connectionId, (int)Double.parseDouble(wifiTraffic), new NetworkInformationManager.OnBandwidthUpdateListener() {
                                @Override
                                public void onSuccess(String result) {
                                    System.out.println(result);
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

            new Thread(new Runnable() {
                public void run(){
                    for(int i = 0; ; i++){
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
// TODO Auto-generated catch block
                            e.printStackTrace();
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
            }).start();

            new Thread(new Runnable() {
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
                            e.printStackTrace();
                        }
                        double frontTime = System.currentTimeMillis();
//  getWifiTraffic(frontTime);
                        double totalWifi02 = getWifiTraffic(frontTime);
                        double errorTraffic = totalWifi02 - totalWifi01;
                        if(errorTraffic < 512){
                            errorTraffic = 1;
                        }
//                        wf += errorTraffic/1111500;
                        wf += errorTraffic;
                        wifiTraffic = df.format(wf);
//  Log.i("使用的流量", wifiTraffic + "");
                        Message message = new Message();
                        message.what = 1;
                        handler.sendMessage(message);
                    }
                }
            }).start();
        }
    }


    public void showNoClientDialog(){
        LayoutInflater factory = LayoutInflater.from(this);
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        final View dialogView = factory.inflate(R.layout.fragment_no_client_dialog, null);
//        final TextView dialogMessage = (TextView) dialogView.findViewById(R.id.oops);
        builder.setView(dialogView);
        builder.setTitle("Oops!");
//        dialogMessage.setText("We didn't find ideal clients for you, please go back to last page and edit your parameters.");
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
//                    final NetworkInformationManager manager = NetworkInformationManager.getInstance(getApplicationContext());

//                    try {
                    // Get nearby client list from backend
//                        manager.findClients(UserInformationManager.token, "school", 1000000, 50000, new NetworkInformationManager.OnFindClientsListener() {
//                            @Override
//                            public void onSuccess(String result) {
                    try {
                        networkInformationManager.requestConnection(UserInformationManager.token, NetworkInformationManager.ssidIdMap.get(selectedWifi.SSID), 1000000, 5000, new NetworkInformationManager.OnRequestConnectionListener() {
                            @Override
                            public void onSuccess(String password, int connectionId) {
                                Log.d(TAG, "password is: " + password);
                                UserInformationManager.connectionId = connectionId;

                                if (selectedWifi.capabilities.contains("WEP")) {
                                    connecting(selectedWifi, password, 2);
                                } else {
                                    connecting(selectedWifi, password, 3);
                                }

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
//                            }

//                            @Override
//                            public void onNetworkFail() {
//                                showEditPwdDialog(selectedWifi);
//                            }
//
//                            @Override
//                            public void onFail() {
//                                showEditPwdDialog(selectedWifi);
//                            }
//                        });
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
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


    private void fillData(String packageName) {
        int uid = PackageManagerHelper.getPackageUid(this, packageName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkStatsManager networkStatsManager = (NetworkStatsManager) getApplicationContext().getSystemService(Context.NETWORK_STATS_SERVICE);
            NetworkStatsHelper networkStatsHelper = new NetworkStatsHelper(networkStatsManager, uid);
            fillNetworkStatsAll(networkStatsHelper);
//            fillNetworkStatsPackage(uid, networkStatsHelper);
        }
//        fillTrafficStatsAll();
//        fillTrafficStatsPackage(uid);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void fillNetworkStatsAll(NetworkStatsHelper networkStatsHelper) {
        long mobileWifiRx = networkStatsHelper.getAllRxBytesMobile(this) + networkStatsHelper.getAllRxBytesWifi();
        long mobileWifiTx = networkStatsHelper.getAllTxBytesMobile(this) + networkStatsHelper.getAllTxBytesWifi();
        System.out.println(mobileWifiRx / 1000000.0 + " MB");
        System.out.println(mobileWifiTx / 1000000.0 + " MB");
    }

//    @TargetApi(Build.VERSION_CODES.M)
//    private void fillNetworkStatsPackage(int uid, NetworkStatsHelper networkStatsHelper) {
//        long mobileWifiRx = networkStatsHelper.getPackageRxBytesMobile(this) + networkStatsHelper.getPackageRxBytesWifi();
//        networkStatsPackageRx.setText(mobileWifiRx / 1000000.0 + " MB");
//        networkStatsPackageRx.setTextColor(Color.parseColor("#FFFFFF"));
//        long mobileWifiTx = networkStatsHelper.getPackageTxBytesMobile(this) + networkStatsHelper.getPackageTxBytesWifi();
//        networkStatsPackageTx.setText(mobileWifiTx / 1000000.0 + " MB");
//        networkStatsPackageTx.setTextColor(Color.parseColor("#FFFFFF"));
//    }
//
//    private void fillTrafficStatsAll() {
//        trafficStatsAllRx.setText(TrafficStatsHelper.getAllRxBytes() / 1000000.0 + " MB");
//        trafficStatsAllTx.setText(TrafficStatsHelper.getAllTxBytes() / 1000000.0 + " MB");
//        trafficStatsAllRx.setTextColor(Color.parseColor("#FFFFFF"));
//        trafficStatsAllTx.setTextColor(Color.parseColor("#FFFFFF"));
//    }
//
//    private void fillTrafficStatsPackage(int uid) {
//        trafficStatsPackageRx.setText(TrafficStatsHelper.getPackageRxBytes(uid) / 1000000.0 + " MB");
//        trafficStatsPackageTx.setText(TrafficStatsHelper.getPackageTxBytes(uid) / 1000000.0 + " MB");
//        trafficStatsPackageRx.setTextColor(Color.parseColor("#FFFFFF"));
//        trafficStatsPackageTx.setTextColor(Color.parseColor("#FFFFFF"));
//    }
}
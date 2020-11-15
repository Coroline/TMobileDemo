package com.example.tmobilenetworkdemo;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.os.Bundle;
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

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tmobilenetworkdemo.Lib.NetworkInformationManager;
import com.example.tmobilenetworkdemo.Lib.UserInformationManager;
import com.example.tmobilenetworkdemo.Wifi.WifiAdmin;

import java.util.List;

public class ConnectHotspotActivity extends AppCompatActivity implements RecyclerViewAdapterNearbyWifi.onWifiSelectedListener {

    private Button scan;
    private WifiAdmin mWifiAdmin;
    private TextView networkStatsAllRx;
    private TextView networkStatsAllTx;
    private TextView currentConnection;
    private RecyclerView wifi_recyclerView;
    private ImageView imageView2;
    List<ScanResult> scanResult;
    List<ScanResult> nearbyClient;
    private String currentSSID;
    public static boolean mIsConnectingWifi=false;
    public static boolean mIsFirstReceiveConnected=false;

    private static final int READ_PHONE_STATE_REQUEST = 37;
    private long connectionStartTime = 0;

    private static final String TAG = "ConnectHotspotActivity";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_hotspot);
        scan = findViewById(R.id.scan);
        currentConnection = findViewById(R.id.current_internet);
        wifi_recyclerView = findViewById(R.id.wifi_recyclerView);
        imageView2 = findViewById(R.id.imageView2);
        wifi_recyclerView.setVisibility(View.INVISIBLE);
        imageView2.setVisibility(View.VISIBLE);

        mWifiAdmin = new WifiAdmin(this);
        currentSSID = mWifiAdmin.getSSID();
        if(currentSSID.equals("NULL"))
            currentConnection.setText("No Internet Connection");
        else
            currentConnection.setText(currentSSID);


        // Get nearby client list from backend
        NetworkInformationManager manager = NetworkInformationManager.getInstance(getApplicationContext());

//        manager.getNearbyClient(currentSSID, new NetworkInformationManager.OnNetworkInformationListener() {
//            @Override
//            public void onSuccess() {
//                // TODO: Assign result list from the network call to nearbyClient ArrayList(line 55)
//            }
//
//            @Override
//            public void onFail() {
//
//            }
//        });
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanResult = mWifiAdmin.getScanResultList();
                System.out.println(scanResult);
//                nearbyClient.retainAll(scanResult);   // Get intersection of scanned result and backend result
                initRecyclerView(scanResult);
                wifi_recyclerView.setVisibility(View.VISIBLE);
                imageView2.setVisibility(View.INVISIBLE);
            }
        });

        // result info:
        // SSID: DoNotConnectMe_5GEXT, BSSID: cc:40:d0:f0:af:38, capabilities: [WPA-PSK-CCMP+TKIP][WPA2-PSK-CCMP+TKIP][WPS][ESS], level: -60, frequency: 5765, timestamp: 524626056217, distance: ?(cm), distanceSd: ?(cm), passpoint: no, ChannelBandwidth: 2, centerFreq0: 5775, centerFreq1: 0, 80211mcResponder: is not supported,

//        fillData(getPackageName());
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
                    //todo: volley request
                    NetworkInformationManager manager = NetworkInformationManager.getInstance(getApplicationContext());
                    manager.checkWifiPassword(selectedWifi.SSID, UserInformationManager.username, new NetworkInformationManager.OnNetworkResultInfoListener() {
                        @Override
                        public void onSuccess(String password) {
                            Log.d(TAG, "password is: " + password);
                            if (selectedWifi.capabilities.contains("WEP")) {
                                connecting(selectedWifi, password,2);
                            }else {
                                connecting(selectedWifi, password,3);
                            }
                        }
                        @Override
                        public void onFail() {
                            showEditPwdDialog(selectedWifi);
                        }
                    });
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


//    private void fillData(String packageName) {
//        int uid = PackageManagerHelper.getPackageUid(this, packageName);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            NetworkStatsManager networkStatsManager = (NetworkStatsManager) getApplicationContext().getSystemService(Context.NETWORK_STATS_SERVICE);
//            NetworkStatsHelper networkStatsHelper = new NetworkStatsHelper(networkStatsManager, uid);
//            fillNetworkStatsAll(networkStatsHelper);
////            fillNetworkStatsPackage(uid, networkStatsHelper);
//        }
////        fillTrafficStatsAll();
////        fillTrafficStatsPackage(uid);
//    }
//
//    @TargetApi(Build.VERSION_CODES.M)
//    private void fillNetworkStatsAll(NetworkStatsHelper networkStatsHelper) {
//        long mobileWifiRx = networkStatsHelper.getAllRxBytesMobile(this) + networkStatsHelper.getAllRxBytesWifi();
//        networkStatsAllRx.setText(mobileWifiRx / 1000000.0 + " MB");
//        long mobileWifiTx = networkStatsHelper.getAllTxBytesMobile(this) + networkStatsHelper.getAllTxBytesWifi();
//        networkStatsAllTx.setText(mobileWifiTx / 1000000.0 + " MB");
//    }

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

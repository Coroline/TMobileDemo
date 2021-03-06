package com.example.tmobilenetworkdemo;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.annotation.RequiresPermission;

import com.example.tmobilenetworkdemo.Lib.GPSTracking;
import com.example.tmobilenetworkdemo.Lib.NetworkInformationManager;
import com.example.tmobilenetworkdemo.Lib.UserInformationManager;
import com.example.tmobilenetworkdemo.Wifi.MyOnStartTetheringCallback;
import com.example.tmobilenetworkdemo.Wifi.WifiAdmin;
import com.example.tmobilenetworkdemo.Wifi.WifiHotUtil;

import org.json.JSONException;

import java.text.ParseException;

/**
 * Activity to let a client create a new hotspot
 */
public class CreateHotspotActivity extends AppCompatActivity {
    private Button createHotspot;
    private EditText SSID;
    private EditText password;
    private EditText bandwidthAmount;
    private EditText sharingDuration;
    private WifiManager wifiManager;
    private WifiConfiguration wifiConfiguration;
    private NetworkInformationManager networkInformationManager;
    private TextView backButtonText;
    private boolean mIsConnectingWifi = false;
    private boolean mIsFirstReceiveConnected = false;
    private WifiAdmin mWifiAdmin;
    private WifiHotUtil mWifiHotUtil;
    private ServiceConnection conn;
    private GPSTracking locationService;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_hotspot);
        createHotspot = findViewById(R.id.createHotspot);
        SSID = findViewById((R.id.ssid));
        password = findViewById(R.id.password);
        bandwidthAmount = findViewById(R.id.amount);
        backButtonText = findViewById(R.id.backButtonText);
        sharingDuration = findViewById(R.id.duration);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        wifiConfiguration = new WifiConfiguration();
        networkInformationManager = NetworkInformationManager.getInstance(getApplicationContext());
        mWifiAdmin = new WifiAdmin(getApplicationContext());
        mWifiHotUtil = WifiHotUtil.getInstance(getApplicationContext());

        backButtonText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back(view);
            }
        });

        requestPermissions();

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

        createHotspot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(isNumeric(bandwidthAmount.getText().toString()) && isNumeric(sharingDuration.getText().toString())) {
                NetworkInformationManager.OnStartSharingListener callback = new NetworkInformationManager.OnStartSharingListener() {
                    @Override
                    public void onSuccess(String result) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Successfully register a hotspot.", Toast.LENGTH_LONG).show();
                                Intent i = new Intent(CreateHotspotActivity.this, CreatedHotspotInformationActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("hotspotName", SSID.getText().toString());
                                i.putExtra("data", bundle);
                                startActivity(i);
                            }
                        });
                    }

                    @Override
                    public void onNetworkFail() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Network Error!", Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onFail() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "unknown error", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                };
                try {
                    networkInformationManager.registerWifiInfo(SSID.getText().toString(), password.getText().toString(), 0.0, 0.0, Double.parseDouble(bandwidthAmount.getText().toString()), Integer.parseInt(sharingDuration.getText().toString()), callback);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Parse error! Please check your input!", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Duration and bandwidth amount must be integers. Please enter your settings again.", Toast.LENGTH_LONG).show();
            }
            }
        });
    }


    /**
     * EditText view input check
     * @param str Entered string
     * @return Whether the entered string is legal or not
     */
    public boolean isNumeric(String str){
        for (int i = 0; i < str.length(); i++){
            if (!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }


    /**
     * Check location permission
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPermissions() {
        if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
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

package com.example.tmobilenetworkdemo.Lib;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;

import com.example.tmobilenetworkdemo.CreateHotspotActivity;
import com.example.tmobilenetworkdemo.CreatedHotspotInformationActivity;
import com.example.tmobilenetworkdemo.MainActivity;

public class GPSTracking extends Service implements LocationListener {
//    private final Context mContext;

    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;

    Location mLocation;
    //    private double lat;
//    private double lng;
    public static double lat;
    public static double lng;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATE = 0;

    private static final long MIN_TIME_BW_UPDATE = 1000 * 6;

    protected LocationManager locationManager;

    private MyBinder binder = new MyBinder();

    public GPSTracking() {
    }


    public Location getLocation() {
        try {
            locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(locationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                //no network or gps
            } else {
                setCanGetLocation(true);

                if (isNetworkEnabled) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        showAlertDialog();
                    }
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            0,
                            5000,
                            this
                    );
                    if (locationManager != null) {
                        mLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (mLocation != null) {
                            setLat(mLocation.getLatitude());
                            setLng(mLocation.getLongitude());
                        }
                    }
                }
                if (isGPSEnabled) {
                    if (mLocation == null) {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            showAlertDialog();
                        }
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATE,
                                MIN_DISTANCE_CHANGE_FOR_UPDATE, this);
                        if (locationManager != null) {
                            mLocation = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (mLocation != null) {
                                setLat(mLocation.getLatitude());
                                setLng(mLocation.getLongitude());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {

        }
        return mLocation;
    }


    public void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle("GPS is settings");

        alertDialog.setMessage("GPS is not enabled. Do you want go to settings menu?");

        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                getApplicationContext().startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }


    public String getAuthorName() {
        return "Yawei Zhang";
    }


    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }


    public class MyBinder extends Binder {
        public GPSTracking getService() {
            return GPSTracking.this;
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();
        System.out.println("''''''''''''''''''''''''''''''''''" + lat + " " + lng);
        Intent i = new Intent(this, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("lat", String.valueOf(lat));
        bundle.putString("lng", String.valueOf(lng));
        i.putExtra("locationData", bundle);

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public boolean isCanGetLocation() {
        return canGetLocation;
    }

    public void setCanGetLocation(boolean canGetLocation) {
        this.canGetLocation = canGetLocation;
    }
}

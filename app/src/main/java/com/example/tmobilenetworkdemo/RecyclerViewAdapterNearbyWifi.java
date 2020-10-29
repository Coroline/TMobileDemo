package com.example.tmobilenetworkdemo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tmobilenetworkdemo.Model.WifiDetail;
import com.example.tmobilenetworkdemo.Wifi.WifiAdmin;

import java.util.List;

public class RecyclerViewAdapterNearbyWifi extends RecyclerView.Adapter<RecyclerViewAdapterNearbyWifi.ViewHolder> {
    private static final String TAG = "RecyclerViewNearbyWifi";

    private List<ScanResult> mWifiDetail;
    private WifiAdmin wifiAdmin;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView wifiName;
        RelativeLayout parentLayout;

        public ViewHolder(View view) {
            super(view);
            wifiName = view.findViewById(R.id.wifi_name);
            parentLayout = itemView.findViewById(R.id.mainLayout);
        }
    }

    public interface onWifiSelectedListener {
        void onWifiSelected(ScanResult selectedWifi);
    }

    private onWifiSelectedListener mListener;

    public RecyclerViewAdapterNearbyWifi(List<ScanResult> wifiDetail, WifiAdmin wifiAdmin, onWifiSelectedListener mListener) {
        this.mWifiDetail = wifiDetail;
        this.wifiAdmin = wifiAdmin;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_wifi_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapterNearbyWifi.ViewHolder holder, final int position) {
        Log.d(TAG, "recycler wifi adapter called");
        holder.wifiName.setText(mWifiDetail.get(position).SSID);
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked on: " + mWifiDetail.get(position).SSID);
                if(mListener != null) {
                    System.out.println("Successfully select a wifi.");
                    mListener.onWifiSelected(mWifiDetail.get(position));
                }
            }
        });
    }


    private void connectSelectedHotspot(ScanResult selectedWifi) {
        boolean isLocked = selectedWifi.capabilities.contains("WEP") || selectedWifi.capabilities.contains("PSK");
        if(!selectedWifi.SSID.equals(wifiAdmin.getSSID()) && !selectedWifi.BSSID.equals(wifiAdmin.getBSSID())) {
            Log.d(TAG, "onItemClick: Selected wifi is not current connected wifi.");
            WifiConfiguration configuration = wifiAdmin.isExsits(selectedWifi.SSID);
            if(configuration == null) {
                Log.d(TAG, "oClick: wifi has not been configured.");
                if(isLocked) {
//                    showEditPwdDialog(selectedWifi);
                } else {
                    Log.d(TAG, "no password.");
                    connecting(selectedWifi, null, 1);
                }
            } else {
                Log.d(TAG, "wifi has been configured.");
                ConnectHotspotActivity.mIsConnectingWifi = true;
                ConnectHotspotActivity.mIsFirstReceiveConnected = true;
                // wifiConnecting
                if(wifiAdmin.connectConfiguration(configuration)) {
                    System.out.println("Connection Error.");
                }
            }
        }
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
        ConnectHotspotActivity.mIsConnectingWifi = true;
        ConnectHotspotActivity.mIsFirstReceiveConnected = true;
        int wcgID = wifiAdmin.connectWifi(result.SSID, pass, type);
        Log.d(TAG, "connecting: " + wcgID);
        if (wcgID == -1) {
            System.out.println("Connection Error.");
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


    @Override
    public int getItemCount() {
        return mWifiDetail.size();
    }
}

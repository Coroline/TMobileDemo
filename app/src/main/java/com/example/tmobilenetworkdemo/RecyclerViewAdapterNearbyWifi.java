package com.example.tmobilenetworkdemo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tmobilenetworkdemo.Model.WifiDetail;
import com.example.tmobilenetworkdemo.Wifi.WifiAdmin;

import java.util.List;


/**
 * RecyclerViewAdapter for nearby qualified hotspot list
 */
public class RecyclerViewAdapterNearbyWifi extends RecyclerView.Adapter<RecyclerViewAdapterNearbyWifi.ViewHolder> {
    private static final String TAG = "RecyclerViewNearbyWifi";

    private List<ScanResult> mWifiDetail;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView wifiName;
        ImageView wifiSignal;
        RelativeLayout parentLayout;

        public ViewHolder(View view) {
            super(view);
            wifiName = view.findViewById(R.id.wifi_name);
            wifiSignal = view.findViewById(R.id.wifi_icon);
            parentLayout = itemView.findViewById(R.id.mainLayout);
        }
    }

    public interface onWifiSelectedListener {
        void onWifiSelected(ScanResult selectedWifi);
    }

    private onWifiSelectedListener mListener;

    public RecyclerViewAdapterNearbyWifi(List<ScanResult> wifiDetail, onWifiSelectedListener mListener) {
        this.mWifiDetail = wifiDetail;
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
        if(mWifiDetail.get(position).capabilities.contains("WEP") || mWifiDetail.get(position).capabilities.contains("PSK")) {
            holder.wifiSignal.setImageResource(R.drawable.wifi_protected);
        }
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

    @Override
    public int getItemCount() {
        return mWifiDetail.size();
    }
}

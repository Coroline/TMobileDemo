package com.example.tmobilenetworkdemo;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tmobilenetworkdemo.Model.WifiDetail;

import java.util.List;

public class RecyclerViewAdapterNearbyWifi extends RecyclerView.Adapter<RecyclerViewAdapterNearbyWifi.ViewHolder> {
    private static final String TAG = "RecyclerViewNearbyWifi";

    private Context context;
    private List<ScanResult> mWifiDetail;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView wifiName;
        RelativeLayout parentLayout;

        public ViewHolder(View view) {
            super(view);
            wifiName = view.findViewById(R.id.wifi_name);
            parentLayout = itemView.findViewById(R.id.mainLayout);
        }
    }


    public RecyclerViewAdapterNearbyWifi(Context context, List<ScanResult> wifiDetail) {
        this.context = context;
        this.mWifiDetail = wifiDetail;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_wifi_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapterNearbyWifi.ViewHolder holder, int position) {
        Log.d(TAG, "recycler wifi adapter called");
        holder.wifiName.setText(mWifiDetail.get(position).SSID);
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }


    @Override
    public int getItemCount() {
        return mWifiDetail.size();
    }
}

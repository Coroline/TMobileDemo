package com.example.tmobilenetworkdemo;

import android.net.wifi.ScanResult;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tmobilenetworkdemo.Model.ConnectDevice;

import java.util.List;

public class RecyclerViewAdapterConnectedUser extends RecyclerView.Adapter<RecyclerViewAdapterConnectedUser.ViewHolder> {
    private static final String TAG = "RecyclerViewAdapterConnectedUser";

    private List<ConnectDevice> mConnectedDevice;

    public interface onDeviceSelectedListener {
        void onDeviceSelected(ConnectDevice selectedDevice);
    }

    private onDeviceSelectedListener mListener;

    public RecyclerViewAdapterConnectedUser(List<ConnectDevice> deviceDetail, onDeviceSelectedListener mListener) {
        this.mConnectedDevice = deviceDetail;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_connected_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.d(TAG, "recycler wifi adapter called");
        holder.deviceName.setText(mConnectedDevice.get(position).getIp());
        holder.deviceDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked on: " + mConnectedDevice.get(position).getIp());
                if(mListener != null) {
                    System.out.println("Successfully select a device.");
                    mListener.onDeviceSelected(mConnectedDevice.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mConnectedDevice.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName;
        ImageView deviceDetail;
        LinearLayout parentLayout;

        public ViewHolder(View view) {
            super(view);
            deviceName = view.findViewById(R.id.device_name);
            deviceDetail = view.findViewById(R.id.deviceDetail);
            parentLayout = itemView.findViewById(R.id.mainLayout);
        }
    }
}

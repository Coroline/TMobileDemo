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

import com.example.tmobilenetworkdemo.Model.ConnectedUserInfo;

import java.text.DecimalFormat;
import java.util.List;

/**
 * RecyclerViewAdapter for connected user list
 */
public class RecyclerViewAdapterConnectedUser extends RecyclerView.Adapter<RecyclerViewAdapterConnectedUser.ViewHolder> {
    private static final String TAG = "RecyclerViewAdapterConnectedUser";

    private List<ConnectedUserInfo> mConnectedUser;

    public interface onConnUserSelectedListener {
        void onConnUserSelected(ConnectedUserInfo selectedConnUser);
    }

    private onConnUserSelectedListener mListener;

    public RecyclerViewAdapterConnectedUser(List<ConnectedUserInfo> deviceDetail, onConnUserSelectedListener mListener) {
        this.mConnectedUser = deviceDetail;
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
        holder.userName.setText(mConnectedUser.get(position).getUsername());

        double total = mConnectedUser.get(position).getBandwidthUsage();
        DecimalFormat df = new DecimalFormat("#.00");
        String output = df.format(total);
        if(total < 1.0) {
            output = "0" + output;
        }

        holder.bandwidthAmount.setText(output + " MB");
        holder.connUserDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked on: " + mConnectedUser.get(position).getUsername());
                if(mListener != null) {
                    System.out.println("Successfully select a connected user.");
                    mListener.onConnUserSelected(mConnectedUser.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mConnectedUser.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        TextView bandwidthAmount;
        ImageView connUserDetail;
        LinearLayout parentLayout;

        public ViewHolder(View view) {
            super(view);
            userName = view.findViewById(R.id.username);
            bandwidthAmount = view.findViewById(R.id.bandwidthAmount);
            connUserDetail = view.findViewById(R.id.deviceDetail);
            parentLayout = itemView.findViewById(R.id.mainLayout);
        }
    }
}

package com.example.tmobilenetworkdemo;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tmobilenetworkdemo.Lib.UserInformationManager;
import com.example.tmobilenetworkdemo.Model.TransactionInfo;
import com.example.tmobilenetworkdemo.Model.User;

import java.text.DecimalFormat;
import java.util.List;

public class RecyclerViewAdapterTransaction extends RecyclerView.Adapter<RecyclerViewAdapterTransaction.ViewHolder> {
    private static final String TAG = "RecyclerViewAdapterTransaction";

    private List<TransactionInfo> mTransactionItem;

    public interface onTransactionSelectedListener {
        void onTransactionSelected(TransactionInfo selectedTransaction);
    }

    private onTransactionSelectedListener mListener;

    public RecyclerViewAdapterTransaction(List<TransactionInfo> transactionDetail, RecyclerViewAdapterTransaction.onTransactionSelectedListener mListener) {
        this.mTransactionItem = transactionDetail;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_transaction_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.d(TAG, "recycler wifi adapter called");
        // TODO: Format timestamp
        holder.date.setText(mTransactionItem.get(position).getTimeStamp());

        String user = mTransactionItem.get(position).getUserName();
        String client = mTransactionItem.get(position).getClientName();
        double credit = mTransactionItem.get(position).getTotalCreditTransferred();
        if(UserInformationManager.username.equals(user)) {    // This transaction is about asking for bandwidth
            holder.interactionEntity.setText(client);
            holder.creditChange.setText("-" + String.format("%.2f", credit) + " credit");
        } else if (UserInformationManager.username.equals(client)) {    // This transaction is about sharing bandwidth
            holder.interactionEntity.setText(user);
            holder.creditChange.setText("+" + String.format("%.2f", credit) + " credit");
        }

        holder.transactionDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked on: " + position);
                if(mListener != null) {
                    System.out.println("Successfully select a transaction item.");
                    mListener.onTransactionSelected(mTransactionItem.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTransactionItem.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView date;
        TextView interactionEntity;
        TextView creditChange;
        ImageView transactionDetail;
        LinearLayout parentLayout;

        public ViewHolder(View view) {
            super(view);
            date = view.findViewById(R.id.date);
            interactionEntity = view.findViewById(R.id.interactionEntity);
            creditChange = view.findViewById(R.id.creditChange);
            transactionDetail = view.findViewById(R.id.transactionDetail);
            parentLayout = itemView.findViewById(R.id.mainLayout);
        }
    }
}

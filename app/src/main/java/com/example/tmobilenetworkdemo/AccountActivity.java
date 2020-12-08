package com.example.tmobilenetworkdemo;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.NoCopySpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tmobilenetworkdemo.Lib.NetworkInformationManager;
import com.example.tmobilenetworkdemo.Lib.UserInformationManager;
import com.example.tmobilenetworkdemo.Model.TransactionInfo;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.SimpleTimeZone;

public class AccountActivity extends AppCompatActivity implements RecyclerViewAdapterTransaction.onTransactionSelectedListener {

    private TextView username;
    private TextView curCredit;
    private LinearLayout home_page;
    private List<TransactionInfo> transactionList = new ArrayList<>();
    private NetworkInformationManager networkInformationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        username = findViewById(R.id.username_account);
        curCredit = findViewById(R.id.credit_account_value);
        home_page = findViewById(R.id.home_page);
        networkInformationManager = NetworkInformationManager.getInstance(getApplicationContext());
        username.setText(UserInformationManager.username);

        home_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(AccountActivity.this, MainActivity.class);
                startActivity(i);
            }
        });
        try {
            networkInformationManager.getCredit(UserInformationManager.token, new NetworkInformationManager.OnCreditQueryListener() {
                @Override
                public void onSuccess(double credit) {
                    curCredit.setText(String.format("%.2f", credit));
                }

                @Override
                public void onNetworkFail() {
                    System.out.println("Get credit query network fail.");
                }

                @Override
                public void onFail() {
                    System.out.println("Get credit query fail.");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            networkInformationManager.getAllReceipts(UserInformationManager.token, new NetworkInformationManager.OnTransactionQueryListener() {
                @Override
                public void onSuccess(List<TransactionInfo> list) {
                    transactionList.addAll(list);
                    initRecyclerView(transactionList);
                }

                @Override
                public void onNetworkFail() {
                    System.out.println("Get all receipt query network fail.");
                }

                @Override
                public void onFail() {
                    System.out.println("Get all receipt query fail.");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        transactionList.add(new TransactionInfo("1", "user", "client", 12.2, 120.0, 1000, "Dec 3, 2020"));
//        transactionList.add(new TransactionInfo("2", "user", "client", 12.3, 130.0, 1045, "Dec 7, 2020"));
//        transactionList.add(new TransactionInfo("3", "client", "user", 12.4, 140.0, 1023, "Dec 18, 2020"));
//        initRecyclerView(transactionList);
    }


    // Recycler View
    private void initRecyclerView(List<TransactionInfo> transactionHistory) {
        RecyclerView recyclerView = findViewById(R.id.transaction_history);
//        System.out.println(transactionHistory.get(1).getTimeStamp());
        RecyclerViewAdapterTransaction adapter = new RecyclerViewAdapterTransaction(transactionHistory, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onTransactionSelected(TransactionInfo selectedTransaction) {
        LayoutInflater factory = LayoutInflater.from(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View dialogView = factory.inflate(R.layout.dialog_transaction_detail, null);
        builder.setView(dialogView);
        builder.setTitle("Transaction Details");
        TextView creditTransferred = dialogView.findViewById(R.id.credit_transfer);
        TextView timestamp = dialogView.findViewById(R.id.transfer_start_date);
        TextView userName = dialogView.findViewById(R.id.user_name);
        TextView clientName = dialogView.findViewById(R.id.client_name);
        TextView bandwidthTransferred = dialogView.findViewById(R.id.bandwidth_transfer);
        TextView duration = dialogView.findViewById(R.id.duration_value);
        creditTransferred.setText(selectedTransaction.getTotalCreditTransferred() + " credit");
        timestamp.setText(String.valueOf(selectedTransaction.getTimeStamp()));
        userName.setText(String.valueOf(selectedTransaction.getUserName()));
        clientName.setText(String.valueOf(selectedTransaction.getClientName()));
        bandwidthTransferred.setText(selectedTransaction.getTotalBandwidthUsage() + " MB");
        // TODO: May need to change duration from second to minutes or hours
        duration.setText(selectedTransaction.getTotalDuration() + " s");
        builder.setPositiveButton("OK", null);
        AlertDialog dialog=builder.create();
        dialog.show();
    }
}

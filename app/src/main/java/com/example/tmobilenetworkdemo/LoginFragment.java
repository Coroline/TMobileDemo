package com.example.tmobilenetworkdemo;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tmobilenetworkdemo.Lib.NetworkInformationManager;
import com.example.tmobilenetworkdemo.Lib.UserInformationManager;

import org.json.JSONException;

import java.util.Objects;

/**
 * Fragment to enter login information including username and password
 */
public class LoginFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private EditText username;
    private EditText password;
    private Button loginButton;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_login, container, false);
        username = v.findViewById(R.id.et_username);
        password = v.findViewById(R.id.et_password);
        loginButton = v.findViewById(R.id.btn_login);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            // If check is successful
            NetworkInformationManager manager = NetworkInformationManager.getInstance(getContext());
            try {
                manager.loginUser(username.getText().toString(), password.getText().toString(), new NetworkInformationManager.OnLoginListener() {
                    @Override
                    public void onSuccess(String token) {
                        System.out.println(token);
                        UserInformationManager.username = username.getText().toString();
                        UserInformationManager.token = token;
                        Intent intent = new Intent();
                        intent.setClass(Objects.requireNonNull(getActivity()), MainActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onNetworkFail() {
                        Toast.makeText(getContext(), "Network Error!", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onAuthFail() {
                        Toast.makeText(getContext(), "Incorrect username or password. Try again", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
            }
        });

        return v;
    }
}
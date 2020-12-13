package com.example.tmobilenetworkdemo;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
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
 * A fragment to enter register information, including full name, username, password
 */
public class RegisterFragment extends Fragment {
    private static final String TAG = "Register Fragment";
    private static final String ARG_PARAM2 = "param2";
    private EditText fullName;
    private EditText username;
    private EditText password;
    private EditText rePassword;
    private Button registerBtn;

    public RegisterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RegisterFragment.
     */
    public static RegisterFragment newInstance(String param1, String param2) {
        RegisterFragment fragment = new RegisterFragment();
        Bundle args = new Bundle();
        args.putString(TAG, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_register, container, false);

        fullName = v.findViewById(R.id.et_name);
        username = v.findViewById(R.id.et_username);
        password = v.findViewById(R.id.et_password);
        rePassword = v.findViewById(R.id.et_repassword);
        registerBtn = v.findViewById(R.id.btn_register);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if (validateForm()) {
                NetworkInformationManager manager = NetworkInformationManager.getInstance(getContext());
                try {
                    manager.registerUser(fullName.getText().toString(), username.getText().toString(), password.getText().toString(), new NetworkInformationManager.OnRegisterUserListener() {
                        @Override
                        public void onSuccess(String token) {
                            Toast.makeText(getContext(), "Registration Successful!", Toast.LENGTH_LONG).show();
                            UserInformationManager.token = token;
                            UserInformationManager.username = username.getText().toString();
                            Intent intent = new Intent();
                            intent.setClass(Objects.requireNonNull(getActivity()), MainActivity.class);
                            startActivity(intent);
                        }

                        @Override
                        public void onRegisterFail() {
                            Toast.makeText(getContext(), "Incomplete register information.", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onNetworkFail() {
                            Toast.makeText(getContext(), "Network Error!", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.d(TAG, "Incomplete register information.");
            }
            }
        });

        return v;
    }


    /**
     * Check EditText Input
     * @return
     */
    private boolean validateForm() {
        boolean valid = true;

        String name = username.getText().toString();
        if (TextUtils.isEmpty(name)) {
            username.setError("Account Name Required.");
            valid = false;
        } else {
            username.setError(null);
        }

        String pwd = password.getText().toString();
        if (TextUtils.isEmpty(pwd)) {
            password.setError("Password Required.");
            valid = false;
        } else {
            password.setError(null);
        }

        String rePwd = rePassword.getText().toString();
        if (TextUtils.isEmpty(rePwd)) {
            rePassword.setError("Password Required.");
            valid = false;
        } else {
            rePassword.setError(null);
        }

        // Check if the username is unique (already done from server side)
        if (pwd.length() < 6) {
            Toast.makeText(getContext(), "Password must be have more than 6 characters.", Toast.LENGTH_LONG).show();
            valid = false;
        }

        if (!rePwd.equals(pwd)) {
            Toast.makeText(getContext(), "Two passwords don't match.", Toast.LENGTH_LONG).show();
            valid = false;
        }

        return valid;
    }
}
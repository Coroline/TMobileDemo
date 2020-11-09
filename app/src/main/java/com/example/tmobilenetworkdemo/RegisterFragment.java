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

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TAG = "Register Fragment";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
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
    // TODO: Rename and change types and number of parameters
    public static RegisterFragment newInstance(String param1, String param2) {
        RegisterFragment fragment = new RegisterFragment();
        Bundle args = new Bundle();
        args.putString(TAG, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }

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
                if(validateForm()) {
                    NetworkInformationManager manager = NetworkInformationManager.getInstance(getContext());
                    manager.storeNewUser(username.getText().toString(), password.getText().toString(), fullName.getText().toString(), new NetworkInformationManager.OnRequestInformationListener() {
                        @Override
                        public void onSuccess(String password) {
                            Intent intent = new Intent();
                            intent.setClass(Objects.requireNonNull(getActivity()), MainActivity.class);
                            startActivity(intent);
                        }

                        @Override
                        public void onFail() {
                            Log.d(TAG, "Incomplete register information.");;
                        }
                    });
                } else {
                    Log.d(TAG, "Incomplete register information.");
                }
            }
        });

        return v;
    }


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

        // TODO: Check if the username is unique

        if(pwd.length() < 6) {
            Toast.makeText(getContext(), "Password must be have more than 6 characters.", Toast.LENGTH_LONG).show();
            valid = false;
        }

        if(!rePwd.equals(pwd)) {
            Toast.makeText(getContext(), "Two passwords don't match.", Toast.LENGTH_LONG).show();
            valid = false;
        }

        return valid;
    }
}
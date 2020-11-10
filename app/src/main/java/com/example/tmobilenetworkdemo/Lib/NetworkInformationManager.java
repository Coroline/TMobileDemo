package com.example.tmobilenetworkdemo.Lib;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class NetworkInformationManager {
    private static final String TAG = "NetworkInformationManager";

    private static NetworkInformationManager sInstance;
    private static Context mContext;
    private static RequestQueue requestQueue;
    private static final String serverUrl = "http://shenjianan97.link:5002";
    private static final String getPath = "login";
    private static final String setPath = "register";

    private NetworkInformationManager(Context context) {
        mContext = context;
        requestQueue = Volley.newRequestQueue(mContext);
    }


    public interface OnRequestInformationListener {
        // change GUI
        void onSuccess(String password);
        // pop up password input prompt
        void onFail();
    }

    public interface OnRegisterInformationListener {
        // proceed to next page
        void onSuccess();
        // close wifi hotspot because of network failure
        void onFail();
    }


    public static NetworkInformationManager getInstance(Context c) {
        if (null == sInstance)
            sInstance = new NetworkInformationManager(c);
        return sInstance;
    }

    public void test() {
        StringRequest stringRequest = new StringRequest(serverUrl, new Response.Listener<String>() {
            //正确接受数据之后的回调
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "yeah");
            }
        }, new Response.ErrorListener() {//发生异常之后的监听回调
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage(), error);
            }
        });
        //将创建的请求添加到请求队列当中
        requestQueue.add(stringRequest);
    }

    public void checkWifiPassword(final String ssid, final String bssid, final OnRequestInformationListener l) {
        String path = "password";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrl+"/"+getPath, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                l.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage(), error);
                l.onFail();
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("ssid", ssid);
                params.put("bssid", bssid);
                return params;
            }
        };
        //将创建的请求添加到请求队列当中
        requestQueue.add(stringRequest);
    }

    public void registerWifiInfo(final String ssid, final String bssid, final String password, final OnRegisterInformationListener l) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrl+"/"+setPath, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                l.onSuccess();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage(), error);
                l.onFail();
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("ssid", ssid);
                params.put("bssid", bssid);
                params.put("password", password);
                return params;
            }
        };
        //将创建的请求添加到请求队列当中
        requestQueue.add(stringRequest);
    }


    // Login: Check is the username and password exist in the backend
    public void checkLogin(final String username, final String password, final OnRequestInformationListener l) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrl+"/"+getPath, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                l.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage(), error);
                l.onFail();
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }


    // Register: Store new user's information into backend
    public void storeNewUser(final String username, final String password, final String fullName, final OnRequestInformationListener l) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrl+"/"+setPath, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                l.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage(), error);
                l.onFail();
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("username", username);
                params.put("password", password);
                params.put("fullname", fullName);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }
}

package com.example.tmobilenetworkdemo.Lib;

import android.content.Context;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NetworkInformationManager {
    private static final String TAG = "NetworkInformationManager";

    private static NetworkInformationManager sInstance;
    private static Context mContext;
    private static RequestQueue requestQueue;
    private static final String serverUrl = "http://34.216.147.160:80";
    private static final String loginPath = "login-app-user";
    private static final String registerUserPath = "register-app-user";
    private static final String registerWifiPath = "registerhotspot";
    private static final String checkWifiPasswordPath = "checkhotspot";

    private NetworkInformationManager(Context context) {
        mContext = context;
        requestQueue = Volley.newRequestQueue(mContext);
    }


    public interface OnNetworkResultInfoListener {
        // change GUI
        void onSuccess(String result);
        // pop up password input prompt
        void onFail();
    }
//
//    public interface OnRegisterHotspotListener {
//        // proceed to next page
//        void onSuccess();
//        // close wifi hotspot because of network failure
//        void onFail();
//    }
//
    public interface OnRegisterUserListener {
        // proceed to next page
        void onSuccess();
        void onFail(String response);
    }

    public interface OnNetworkInformationListener {
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

    public void checkWifiPassword(final String ssid, final String username, final OnNetworkResultInfoListener l) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrl+"/"+ checkWifiPasswordPath, new Response.Listener<String>() {
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
                params.put("username", username);
                return params;
            }
        };
        //将创建的请求添加到请求队列当中
        requestQueue.add(stringRequest);
    }

    public void registerWifiInfo(final String ssid, final String password, final String username, final OnNetworkInformationListener l) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrl+"/"+ registerWifiPath, new Response.Listener<String>() {
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
                params.put("password", password);
                params.put("username", username);
                return params;
            }
        };
        //将创建的请求添加到请求队列当中
        requestQueue.add(stringRequest);
    }


    // Login: Check is the username and password exist in the backend
//    public void checkLogin(final String username, final String password, final OnRequestHotspotInfoListener l) {
//        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrl+"/"+ loginPath, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                l.onSuccess(response);
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.e(TAG, error.getMessage(), error);
//                l.onFail();
//            }
//        }){
//            @Override
//            protected Map<String,String> getParams(){
//                Map<String,String> params = new HashMap<>();
//                params.put("username", username);
//                params.put("password", password);
//                return params;
//            }
//        };
//        requestQueue.add(stringRequest);
//    }


    /**
     * Register
     * @param username username for register
     * @param password password for register
     * @param l interface for callback functions
     * @throws JSONException
     */
    public void registerUser(final String username, final String password, final OnNetworkInformationListener l) throws JSONException {

        JSONObject credential = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        credential.put("username", username);
        credential.put("password", password);
        jsonObject.put("credential", credential);

        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, serverUrl+"/"+ registerUserPath, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        l.onSuccess();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage(), error);
            }
        }) {

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json; charset=UTF-8");
                headers.put("Connection", "keep-alive");
                return headers;
            }
        };
        requestQueue.add(jsonRequest);
    }


    /**
     * Login network request
     * @param username username for login
     * @param password password for login
     * @param l interface for callback functions
     * @throws JSONException
     */
    public void loginUser(final String username, final String password, final OnNetworkResultInfoListener l) throws JSONException {

        JSONObject credential = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        credential.put("username", username);
        credential.put("password", password);
        jsonObject.put("credential", credential);

        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, serverUrl+"/"+ loginPath, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Login response -> " + response.toString());
                        try {
                            l.onSuccess(response.getString("token"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage(), error);
            }
        }) {

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json; charset=UTF-8");
                return headers;
            }
        };
        requestQueue.add(jsonRequest);
    }


    // Get nearby client list for future connection
    public void getNearbyClient(final String SSID, final OnNetworkInformationListener l) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrl+"/"+ registerUserPath, new Response.Listener<String>() {
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
                params.put("SSID", SSID);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }
}

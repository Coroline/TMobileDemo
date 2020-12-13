package com.example.tmobilenetworkdemo.Lib;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.tmobilenetworkdemo.Model.ConnectedUserInfo;
import com.example.tmobilenetworkdemo.Model.TransactionInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for all network calls with web server
 */
public class NetworkInformationManager {
    private static final String TAG = "NetworkInformationManager";

    private static NetworkInformationManager sInstance;
    private static Context mContext;
    private static RequestQueue requestQueue;
    private static final String serverUrl = "http://ec2-3-236-150-225.compute-1.amazonaws.com:80";
    private static final String loginPath = "login-app-user";
    private static final String registerUserPath = "register-app-user";
    private static final String startSharingPath = "start-sharing";
    private static final String stopSharingPath = "stop-sharing";
    private static final String disconnectUserPath = "disconnect-user";
    private static final String findClientsPath = "find-clients";
    private static final String requestConnectionPath = "request-connection-to-client";
    private static final String updateBandwidthUsage = "update-bandwidth-usage";
    private static final String queryBandwidthUsage = "query-bandwidth-usage";
    private static final String clientUpdateLocation = "client-update-location";
    private static final String getAllReceipts = "get-all-receipts";
    private static final String queryCredit = "query-credit";
    private static final String queryConnectionBandwidthUsage = "query-connection-bandwidth-usage";
    public static HashMap<String, String> ssidIdMap = new HashMap<>();

    private NetworkInformationManager(Context context) {
        mContext = context;
        requestQueue = Volley.newRequestQueue(mContext);
    }

    public interface OnRequestConnectionListener {
        void onSuccess(String password, int connectionId);

        void onNetworkFail();

        void onFail();
    }

    public interface OnFindClientsListener {
        void onSuccess(HashMap<String, String> result);

        void onNetworkFail();

        void onFail();
    }

    public interface OnRegisterUserListener {
        // proceed to next page
        void onSuccess(String token);

        void onNetworkFail();

        void onRegisterFail();
    }

    public interface OnLoginListener {
        // proceed to next page
        void onSuccess(String token);

        void onNetworkFail();

        void onAuthFail();
    }

    public interface OnStartSharingListener {
        // proceed to next page
        void onSuccess(String result);

        // close wifi hotspot because of network failure
        void onNetworkFail();

        void onFail();
    }

    public interface OnBandwidthUpdateListener {
        void onSuccess(String result);

        void onNetworkFail();

        void onFail();
    }

    public interface OnBandwidthQueryListener {
        void onSuccess(List<ConnectedUserInfo> list, double clientCredit);

        void onNetworkFail();

        void onFail();
    }

    public interface OnClientLocationUpdateListener {
        void onSuccess(String result);

        void onNetworkFail();

        void onFail();
    }

    public interface OnTransactionQueryListener {
        void onSuccess(List<TransactionInfo> list);

        void onNetworkFail();

        void onFail();
    }

    public interface OnCreditQueryListener {
        void onSuccess(double credit);

        void onNetworkFail();

        void onFail();
    }

    public interface OnStopSharingListener {
        void onSuccess(Boolean status);

        void onNetworkFail();

        void onFail();
    }

    public interface OnDisconnectUserListener {
        void onSuccess(Boolean status);

        void onNetworkFail();

        void onFail();
    }

    public interface OnConnectionBandwidthUsageListener{
        void onSuccess(double bandwidthUsed, int duration, double creditsTransferred);

        void onNetworkFail();

        void onFail();
    }

    public static NetworkInformationManager getInstance(Context c) {
        if (null == sInstance)
            sInstance = new NetworkInformationManager(c);
        return sInstance;
    }


    /**
     * User requests a network connection with a client to use his bandwidth resources
     * @param token User identifier
     * @param clientUsername Username of the client
     * @param bandwidth Bandwidth amount that the user wants
     * @param duration Duration that the user wants
     * @param l Event listener
     * @throws JSONException
     */
    public void requestConnection(final String token, final String clientUsername, final double bandwidth, final int duration, final OnRequestConnectionListener l) throws JSONException{
        /*
        {
          "token": "string",
          "clientUsername": "string",
          "sharingConfiguration": {
            "bandwidth": 0,
            "duration": 0
          }
        }
         */
        JSONObject jsonObject = new JSONObject();
        JSONObject sharingConfiguration = new JSONObject();
        sharingConfiguration.put("bandwidth", bandwidth);
        sharingConfiguration.put("duration", duration);
        jsonObject.put("token", token);
        jsonObject.put("clientUsername", clientUsername);
        jsonObject.put("sharingConfiguration", sharingConfiguration);

        Log.d(TAG, "request connection -> " + jsonObject.toString());

        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, serverUrl + "/" + requestConnectionPath, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "request connection response -> " + response.toString());
                        try {
                            l.onSuccess(response.getString("password"), response.getInt("connectionId"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NetworkError) {
                    l.onNetworkFail();
                } else {
                    l.onFail();
                    Log.e(TAG, error.getMessage(), error);
                }
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


    /**
     * After a client create a hotspot, add this new hotspot's information into the web server
     * @param ssid SSID of new hotspot
     * @param password password of new hotspot
     * @param latitude Latitude of the client location
     * @param longitude Longitude of the client location
     * @param bandwidth Bandwidth amount that the client would like to share with users
     * @param duration Duration that the client would like to share with users
     * @param l Event listener
     * @throws JSONException
     */
    public void registerWifiInfo(final String ssid, final String password, final double latitude, final double longitude, final double bandwidth, final int duration, final OnStartSharingListener l) throws JSONException {
        /*
        {
          "token": "Afdsfqe124",
          “Location”: “school”,
          "hotspotInformation": {
            "ssid": "123",
            "pwd": "abc"
          },
          "sharingConfiguration": {
            "bandwidth": 123.0,
            "duration": 100
          }
        }
         */
        JSONObject hotspotInformation = new JSONObject();
        JSONObject sharingConfiguration = new JSONObject();
        JSONObject location = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        hotspotInformation.put("ssid", ssid);
        hotspotInformation.put("pwd", password);
        sharingConfiguration.put("bandwidth", bandwidth);
        sharingConfiguration.put("duration", duration);
        location.put("latitude", latitude);
        location.put("longitude", longitude);
        jsonObject.put("token", UserInformationManager.token);
        jsonObject.put("location", location);
        jsonObject.put("hotspotInformation", hotspotInformation);
        jsonObject.put("sharingConfiguration", sharingConfiguration);

        Log.d(TAG, jsonObject.toString());

        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, serverUrl + "/" + startSharingPath, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "register wifi response -> " + response.toString());
                        try {
                            l.onSuccess(response.getString("status"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NetworkError) {
                    l.onNetworkFail();
                } else {
                    l.onFail();
                    Log.e(TAG, error.getMessage(), error);
                }
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


    /**
     * Return a list of nearby qualified clients for the user
     * @param token User identifier
     * @param latitude latitude of the user's current location
     * @param longitude Longitude of the user's current location
     * @param bandwidth Max bandwidth amount that the user would like to consume
     * @param duration Max duration that the user want to connect with a client
     * @param l Event listener
     * @throws JSONException
     */
    public void findClients(final String token, final double latitude, final double longitude, final int bandwidth, final int duration, final OnFindClientsListener l) throws JSONException{
        /*
        {
          "token": "Afdsfqe124",
          "location": "Cafe",
          "sharingConfiguration": {
            "bandwidth": 123, // bytes
            "duration": 3600 // seconds
          }
        }
         */
        JSONObject sharingConfiguration = new JSONObject();
        sharingConfiguration.put("bandwidth", bandwidth);
        sharingConfiguration.put("duration", duration);
        JSONObject location = new JSONObject() ;
        location.put("latitude", latitude);
        location.put("longitude", longitude);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", token);
        jsonObject.put("location", location);
        jsonObject.put("sharingConfiguration", sharingConfiguration);

        Log.d(TAG, "find client request -> " + jsonObject.toString());
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, serverUrl + "/" + findClientsPath, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "find clients response -> " + response.toString());
                        JSONArray ssidArray = response.optJSONArray("ssids");
                        JSONArray namesArray = response.optJSONArray("clientUsernames");
                        String[] ssids = new String[ssidArray.length()];
                        for (int i = 0; i < ssids.length; ++i) {
                            ssids[i] = ssidArray.optString(i);
                        }
                        String[] names = new String[namesArray.length()];
                        for (int i = 0; i < names.length; ++i) {
                            names[i] = namesArray.optString(i);
                        }
                        for(int i = 0; i < ssids.length; i++) {
                            ssidIdMap.put(ssids[i], names[i]);
                        }
                        l.onSuccess(ssidIdMap);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NetworkError) {
                    l.onNetworkFail();
                } else {
                    l.onFail();
                    Log.e(TAG, error.getMessage(), error);
                }
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


    /**
     * Update and send bandwidth usage width a specific time interval to the web server
     * @param token User's identifier
     * @param connectionID Hotspot connection identifier
     * @param bandwidthUsageUpdate Bandwidth usage amount to add
     * @param l
     */
    public void updateBandwidthUsage(final String token, final int connectionID, final double bandwidthUsageUpdate, final OnBandwidthUpdateListener l) throws JSONException {
        JSONObject updateBandwidthInfo = new JSONObject();
        updateBandwidthInfo.put("token", token);
        updateBandwidthInfo.put("connectionId", connectionID);
        updateBandwidthInfo.put("bandwidthUsageSinceLastUpdate", bandwidthUsageUpdate);

        Log.d(TAG, "update request: " + updateBandwidthInfo.toString());

        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, serverUrl + "/" + updateBandwidthUsage, updateBandwidthInfo,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "update bandwidth response -> " + response.toString());
                        try {
                            l.onSuccess(response.getString("shouldDisconnect"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error == null || error.networkResponse == null) {
                    return;
                }

                String body;
                //get status code here
                final String statusCode = String.valueOf(error.networkResponse.statusCode);
                //get response body and parse with appropriate encoding
                try {
                    body = new String(error.networkResponse.data,"UTF-8");
                    Log.d(TAG, "status code -> " + statusCode);
                    Log.d(TAG, "body -> " + body);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (statusCode.equals("400")){
                    // should disconnect
                    l.onSuccess("true");
                    return;
                }
                if (error instanceof NetworkError) {
                    l.onNetworkFail();
                } else if (error instanceof AuthFailureError) {
                    l.onFail();
                } else {
                    Log.e(TAG, error.getMessage(), error);
                }
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


    /**
     * Client query connected users and their detailed information after create a hotspot
     * @param token User's identifier
     * @param l     Interface for callback functions
     * @throws JSONException
     */
    public void queryBandwidthUsage(final String token, final OnBandwidthQueryListener l) throws JSONException {
        JSONObject queryBandwidthInfo = new JSONObject();
        queryBandwidthInfo.put("token", token);

        Log.d(TAG, "client query bandwidth usage request: " + queryBandwidthInfo.toString());

        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, serverUrl + "/" + queryBandwidthUsage, queryBandwidthInfo,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "client query bandwidth response -> " + response.toString());
                        JSONArray usernameArray = response.optJSONArray("usernameList");
                        JSONArray connectionIdsArray = response.optJSONArray("connectionIds");
                        JSONArray bandwidthUsageArray = response.optJSONArray("bandwidthUsageList");
                        JSONArray durationArray = response.optJSONArray("durationList");
                        Double clientCredit = response.optDouble("clientCredit");
                        int len = usernameArray.length();
                        List<ConnectedUserInfo> connUserList = new ArrayList<>();
                        for(int i = 0; i < len; i++) {
                            try {
                                connUserList.add(new ConnectedUserInfo(usernameArray.optString(i), Integer.parseInt(connectionIdsArray.optString(i)), Double.parseDouble(bandwidthUsageArray.optString(i)), Integer.parseInt(durationArray.optString(i))));
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                        l.onSuccess(connUserList, clientCredit);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NetworkError) {
                    l.onNetworkFail();
                } else if (error instanceof AuthFailureError) {
                    l.onFail();
                } else {
                    Log.e(TAG, error.getMessage(), error);
                }
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


    /**
     * Client sends its latest location information to the web server
     * @param token Client's identifier
     * @param lat   Updated latitude
     * @param lng   Updated longitude
     * @param l     Interface for callback functions
     * @throws JSONException
     */
    public void updateClientLocation(final String token, final double lat, final double lng, final OnClientLocationUpdateListener l) throws JSONException {
        JSONObject location = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        location.put("latitude", "0");
        location.put("longitude", "0");
        jsonObject.put("location", location);
        jsonObject.put("token", token);

        Log.d(TAG, "client update location -> " + jsonObject.toString());
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, serverUrl + "/" + clientUpdateLocation, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "client update location response -> " + response.toString());
                        l.onSuccess(response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error == null || error.networkResponse == null) {
                    return;
                }
                String body;
                //get status code here
                final String statusCode = String.valueOf(error.networkResponse.statusCode);
                //get response body and parse with appropriate encoding
                try {
                    body = new String(error.networkResponse.data,"UTF-8");
                    Log.d(TAG, "status code -> " + statusCode);
                    Log.d(TAG, "body -> " + body);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (error instanceof NetworkError) {
                    l.onNetworkFail();
                } else if (error instanceof AuthFailureError) {
                    l.onFail();
                } else {
                    Log.e(TAG, error.getMessage(), error);
                }
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
        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonRequest);
    }


    /**
     * Retrieve all transaction history from the web server
     * @param token Client or user identifier
     * @param l     Interface for callback functions
     * @throws JSONException
     */
    public void getAllReceipts(final String token, final OnTransactionQueryListener l) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", token);

        Log.d(TAG, "get all receipts -> " + jsonObject.toString());
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, serverUrl + "/" + getAllReceipts, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "transaction query response -> " + response.toString());
                        JSONArray receiptArray = response.optJSONArray("receipts");
                        int len = receiptArray.length();
                        List<TransactionInfo> transactionList = new ArrayList<>();
                        for(int i = 0; i < len; i++) {
                            try {
                                JSONObject item = receiptArray.getJSONObject(i);
                                transactionList.add(new TransactionInfo(item.getString("sessionId"), item.getString("userName"), item.getString("clientName"), Double.parseDouble(item.getString("totalBandwidth")), Double.parseDouble(item.getString("totalCreditsTransferred")), Integer.parseInt(item.getString("totalDuration"))/1000, item.getString("timestamp")));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        l.onSuccess(transactionList);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error == null || error.networkResponse == null) {
                    return;
                }
                String body;
                //get status code here
                final String statusCode = String.valueOf(error.networkResponse.statusCode);
                //get response body and parse with appropriate encoding
                try {
                    body = new String(error.networkResponse.data,"UTF-8");
                    Log.d(TAG, "status code -> " + statusCode);
                    Log.d(TAG, "body -> " + body);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (error instanceof NetworkError) {
                    l.onNetworkFail();
                } else if (error instanceof AuthFailureError) {
                    l.onFail();
                } else {
                    Log.e(TAG, error.getMessage(), error);
                }
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


    /**
     * Query current credit
     * @param token Client or user identifier
     * @param l     Interface for callback functions
     * @throws JSONException
     */
    public void getCredit(final String token, final OnCreditQueryListener l) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", token);

        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, serverUrl + "/" + queryCredit, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "credit query response -> " + response.toString());
                        try {
                            l.onSuccess(response.getDouble("credit"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NetworkError) {
                    l.onNetworkFail();
                } else if (error instanceof AuthFailureError) {
                    l.onFail();
                } else {
                    Log.e(TAG, error.getMessage(), error);
                }
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


    /**
     * Stop sharing bandwidth
     * @param token Client or user identifier
     * @param l     Interface for callback functions
     * @throws JSONException
     */
    public void stopSharing(final String token, final OnStopSharingListener l) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", token);

        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, serverUrl + "/" + stopSharingPath, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "stop sharing response -> " + response.toString());
                        try {
                            l.onSuccess(Boolean.parseBoolean(response.getString("status")));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NetworkError) {
                    l.onNetworkFail();
                } else if (error instanceof AuthFailureError) {
                    l.onFail();
                } else {
                    Log.e(TAG, error.getMessage(), error);
                }
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


    /**
     * Disconnect user
     * @param token Client or user identifier
     * @param connectionID Identifier of hotspot connection
     * @param l     Interface for callback functions
     * @throws JSONException
     */
    public void disconnectUser(final String token, final int connectionID, final OnDisconnectUserListener l) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", token);
        jsonObject.put("connectionId", connectionID);


        Log.d(TAG, "disconnect request -> " + jsonObject.toString());
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, serverUrl + "/" + disconnectUserPath, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "disconnect user response -> " + response.toString());
                        try {
                            l.onSuccess(Boolean.parseBoolean(response.getString("status")));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NetworkError) {
                    l.onNetworkFail();
                } else if (error instanceof AuthFailureError) {
                    l.onFail();
                } else {
                    Log.e(TAG, error.getMessage(), error);
                }
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


    /**
     * Register
     *
     * @param username username for register
     * @param password password for register
     * @param l        interface for callback functions
     * @throws JSONException
     */
    public void registerUser(final String fullName, final String username, final String password, final OnRegisterUserListener l) throws JSONException {
        /*
        {
          "credential": {
            "username": "string",
            "password": "string"
          },
          "name": "string"
        }
         */
        JSONObject credential = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        credential.put("username", username);
        credential.put("password", password);
        jsonObject.put("credential", credential);
        jsonObject.put("name", fullName);

        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, serverUrl + "/" + registerUserPath, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "register response -> " + response.toString());
                        try {
                            l.onSuccess(response.getString("token"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NetworkError) {
                    l.onNetworkFail();
                } else if (error instanceof AuthFailureError) {
                    l.onRegisterFail();
                } else {
                    Log.e(TAG, error.getMessage(), error);
                }
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


    /**
     * Login network request
     *
     * @param username username for login
     * @param password password for login
     * @param l        interface for callback functions
     * @throws JSONException
     */
    public void loginUser(final String username, final String password, final OnLoginListener l) throws JSONException {

        JSONObject credential = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        credential.put("username", username);
        credential.put("password", password);
        jsonObject.put("credential", credential);

        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, serverUrl + "/" + loginPath, jsonObject,
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
                if (error instanceof NetworkError) {
                    Log.e(TAG, error.getMessage(), error);
                    l.onNetworkFail();
                } else if (error instanceof AuthFailureError) {
                    l.onAuthFail();
                } else {
                    Log.e(TAG, error.getMessage(), error);
                }
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


    /**
     * Return the bandwidth amount given a network connection ID
     * @param token User identifier
     * @param connectionId Network connection ID
     * @param l
     * @throws JSONException
     */
    public void queryConnectionBandwidthUsage(final String token, final int connectionId, final OnConnectionBandwidthUsageListener l) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", token);
        jsonObject.put("connectionId", connectionId);

        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, serverUrl + "/" + queryConnectionBandwidthUsage, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "query connection bandwidth usage response -> " + response.toString());
                        try {
                            l.onSuccess(response.getDouble("bandwidthUsed"), response.getInt("duration"), response.getDouble("creditsTransferred"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error == null || error.networkResponse == null) {
                    return;
                }
                String body;
                //get status code here
                final String statusCode = String.valueOf(error.networkResponse.statusCode);
                //get response body and parse with appropriate encoding
                try {
                    body = new String(error.networkResponse.data,"UTF-8");
                    Log.d(TAG, "status code -> " + statusCode);
                    Log.d(TAG, "body -> " + body);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (error instanceof NetworkError) {
                    l.onNetworkFail();
                } else if (error instanceof AuthFailureError) {
                    l.onFail();
                } else {
                    Log.e(TAG, error.getMessage(), error);
                }
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
}

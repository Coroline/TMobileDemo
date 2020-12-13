package com.example.tmobilenetworkdemo.Wifi;

/**
 * ScanResult class store wifi information via scan
 * public String      BSSID
 * public String      SSID
 * public String      capabilities      Network performance (authentication supported by access points, key management, encryption mechanisms...)
 * public int            frequency
 * public int            level          signal intensity
 *
 */
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * WifiConfiguration
 * AuthAlgorithm        get IEEE 802.11 encryption method
 * PairwiseCipher         WPA key pair
 * Protocol               Encryption protocol
 * KeyMgmt                Password management system
 * GroupCipher
 * Status                 Current network status
 */

public class WifiAdmin {
    //Tag
    private static final String TAG = "WifiAdmin";

    //String buffer
    private StringBuffer mStringBuffer=new StringBuffer();

    //store scan result
    public List<ScanResult> mScanResultList=new ArrayList<>();

    private ScanResult mScanResult;

    //create WifiManager object
    private WifiManager mWifiManager;

    //create WifiInfo object
    private WifiInfo mWifiInfo;

    //network connection list
    private List<WifiConfiguration> mWifiConfigurationList=new ArrayList<>();

    //create WifiLock
    private WifiManager.WifiLock mWifiLock;

    private ConnectivityManager mConnectivityManager;

    private Context mContext;

    //Wifi configuration list (with pwd)
    private List<WifiConfiguration> mWifiConfigedSpecifiedList=new ArrayList<>();

    private NetworkInfo.State mState;


    /**
     * Constructor
     * @param context
     */
    public WifiAdmin(Context context){
        mContext = context;
        mWifiManager = (WifiManager) mContext.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        mWifiInfo = mWifiManager.getConnectionInfo();
        mConnectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        againGetWifiInfo();
    }

    /**
     * Retrieves current Wifi connection information
     */
    public void againGetWifiInfo(){
        mWifiInfo = isNetCardFriendly()?mWifiManager.getConnectionInfo():null;
    }


    public void againGetWifiConfigurations(){
        mWifiConfigurationList = mWifiManager.getConfiguredNetworks();
    }

    /**
     * Determines whether the user has Wifi card on. True is on
     * @return
     */
    public boolean isNetCardFriendly(){
        return mWifiManager.isWifiEnabled();
    }


    public boolean isConnecting(){
        mState = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .getState();
        return NetworkInfo.State.CONNECTING == mState;
    }


    public boolean isConnected(){
        mState = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .getState();
        return NetworkInfo.State.CONNECTED == mState;
    }

    /**
     * get current network connection status
     * @return
     */
    public NetworkInfo.State getCurrentState(){
        return (mState = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .getState());
    }

    /**
     * Set up the configured network (with password)
     * @param ssid
     */
    public void setWifiConfigedSpecifiedList(String ssid){
        mWifiConfigedSpecifiedList.clear();
        if (mWifiConfigurationList != null) {
            for (WifiConfiguration configuration : mWifiConfigurationList) {
                if (configuration.SSID.equalsIgnoreCase("\""+ssid+"\"")
                        &&configuration.preSharedKey!=null) {
                    mWifiConfigedSpecifiedList.add(configuration);
                }
            }
        }
    }

    //return wifi setup list
    public List<WifiConfiguration> getWifiConfigedSpecifiedList() {
        return mWifiConfigedSpecifiedList;
    }

    public void openNetCard(){
        WifiHotUtil wifiHotUtil=WifiHotUtil.getInstance(mContext);
        if (wifiHotUtil.getWifiAPState()==WifiHotUtil.WIFI_AP_STATE_ENABLED) {
            wifiHotUtil.closeWifiAp();
        }
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    public void closeNetCard(){
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
    }

    public void checkNetCardState(){
        switch (mWifiManager.getWifiState()) {
            case 0:
                Log.i(TAG, "checkNetCardState: closing net card");
                break;
            case 1:
                Log.i(TAG, "checkNetCardState: closed net card");
                break;
            case 2:
                Log.i(TAG, "checkNetCardState: opening net card");
                break;
            case 3:
                Log.i(TAG, "checkNetCardState: opened net card");
                break;
            default:
                Log.i(TAG, "checkNetCardState: no status");
                break;
        }
    }

    //scan nearby network
    public void scan(){
        mWifiManager.startScan();
    }

    //get scan result
    public List<ScanResult> getScanResultList() {
        List<ScanResult> results=mWifiManager.getScanResults();
        mScanResultList.clear();
        if (results == null) {
            Log.i(TAG, "scan: no wifi exist");
        }else {
            Log.i(TAG, "scan: get wifi    "+results.size());
            for(ScanResult result : results){
                if (result.SSID == null || result.SSID.length() == 0
                        || result.capabilities.contains("[IBSS]")) {
                    continue;
                }
                boolean found = false;
                for(ScanResult item:mScanResultList){
                    if(item.SSID.equals(result.SSID)&&item.capabilities.equals(result.capabilities)){
                        found = true;
                        break;
                    }
                }
                if(!found){
                    mScanResultList.add(result);
                }
            }
        }
        return mScanResultList;
    }

    public String getScanResult(){
        //Clear the last result before each scan click
        if (mStringBuffer != null) {
            mStringBuffer=new StringBuffer();
        }
        if (mScanResultList != null) {
            for (int i = 0; i < mScanResultList.size(); i++) {
                mScanResult=mScanResultList.get(i);
                mStringBuffer=mStringBuffer.append("No.").append(i+1)
                        .append(":").append(mScanResult.SSID).append("->")
                        .append(mScanResult.BSSID).append("->")
                        .append(mScanResult.capabilities).append("->")
                        .append(mScanResult.frequency).append("->")
                        .append(mScanResult.level).append("->")
                        .append(mScanResult.describeContents())
                        .append("\n\n");
            }
        }
        Log.i(TAG, "getScanResult: "+mStringBuffer.toString());
        return mStringBuffer.toString();
    }


    public void disconnectWifi(){
        //get net ID
        int netId = getNetWorkId();
        //Set the network to be unavailable
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
        mWifiInfo=null;
    }

    public void acquireWifiLock(){
        mWifiLock.acquire();
    }

    public void releaseWifiLock(){
        if (mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }
    }

    public void creatWifiLock(){
        mWifiLock = mWifiManager.createWifiLock("WifiSharing");
    }


    public List<WifiConfiguration> getWifiConfigurationList() {
        return mWifiConfigurationList;
    }

    public boolean connectConfiguration(int index){
        if (index>=mWifiConfigurationList.size()) {
            return false;
        }else {
            return mWifiManager.enableNetwork(mWifiConfigurationList.get(index).networkId,
                    true);
        }
    }

    public boolean connectConfiguration(WifiConfiguration wcf){

        return mWifiManager.enableNetwork(wcf.networkId,
                true);
    }


    public int getNetWorkId(){
        return (mWifiInfo==null)?0:mWifiInfo.getNetworkId();
    }

    public int getIpAddress(){
        return (mWifiInfo==null)?0:mWifiInfo.getIpAddress();
    }

    public int getRssi(){
        return (mWifiInfo==null)?0:mWifiInfo.getRssi();
    }

    public String getMacAddress(){
        return (mWifiInfo==null)?"NULL":mWifiInfo.getMacAddress();
    }

    public String getBSSID(){
        return (mWifiInfo==null)?"NULL":mWifiInfo.getBSSID();
    }

    public String getSSID(){
        return (mWifiInfo==null)?"NULL":mWifiInfo.getSSID().substring(1,mWifiInfo.getSSID().length()-1);
    }

    public WifiInfo getWifiInfo(){
        return mWifiInfo;
    }


    public int addNetwork(WifiConfiguration wcg){
        int wcgID=mWifiManager.addNetwork(wcg);
        mWifiManager.enableNetwork(wcgID,true);
        return wcgID;
    }

    //create Wifi hotspot
    public int connectWifi(String SSID, String Password, int Type) {
        Log.d(TAG, "createWifiInfo: not exist"+SSID);
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.status = WifiConfiguration.Status.DISABLED;
        wifiConfig.priority = 40;
        wifiConfig.SSID = "\"" + SSID + "\"";
        //no pwd
        if(Type == 1) {//WIFICIPHER_NOPASS
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wifiConfig.allowedAuthAlgorithms.clear();
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        }
        //has pwd
        if(Type == 2){ //WIFICIPHER_WEP
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

            if (getHexKey(Password)) wifiConfig.wepKeys[0] = Password;
            else wifiConfig.wepKeys[0] = "\"".concat(Password).concat("\"");
            wifiConfig.wepTxKeyIndex = 0;

        }
        if(Type == 3) {//WIFICIPHER_WPA
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wifiConfig.preSharedKey = "\"".concat(Password).concat("\"");
        }

        //connect
        int wcgID = mWifiManager.addNetwork(wifiConfig);
        mWifiManager.enableNetwork(wcgID,true);
        //update setting
        againGetWifiConfigurations();
        againGetWifiInfo();
        return wcgID;
    }

    public void forgetWifi(int netId){
        mWifiManager.removeNetwork(netId);
        mWifiManager.saveConfiguration();
    }

    public WifiConfiguration isExsits(String SSID)
    {
        againGetWifiConfigurations();
        for (WifiConfiguration existingConfig : mWifiConfigurationList)
        {
            if (existingConfig.SSID.equals("\""+SSID+"\""))
            {
                return existingConfig;
            }
        }
        return null;
    }

    private static boolean getHexKey(String s) {
        if (s == null) {
            return false;
        }

        int len = s.length();
        if (len != 10 && len != 26 && len != 58) {
            return false;
        }

        for (int i = 0; i < len; ++i) {
            char c = s.charAt(i);
            if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {
                continue;
            }
            return false;
        }
        return true;
    }
}

/**
 * Reference: https://github.com/biloba123/WifiSharing2
 */

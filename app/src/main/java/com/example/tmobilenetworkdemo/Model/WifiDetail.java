package com.example.tmobilenetworkdemo.Model;

public class WifiDetail {
    private String SSID;
    private String BSSID;
    private String capabilities;
    private String level;
    private String frequency;
    private String timestamp;
    private String ChannelBandwidth;

    public WifiDetail(String SSID, String BSSID) {
        this.SSID = SSID;
        this.BSSID = BSSID;
    }

    public String getSSID() {return SSID;}
    public void setSSID(String SSID) {this.SSID = SSID;}
    public String getBSSID() {return BSSID;}
    public void setBSSID(String BSSID) {this.BSSID = BSSID;}

}

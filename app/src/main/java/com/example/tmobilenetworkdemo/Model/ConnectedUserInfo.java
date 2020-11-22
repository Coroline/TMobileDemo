package com.example.tmobilenetworkdemo.Model;

public class ConnectedUserInfo {
    public String username;
    public int connectionId;
    public int bandwidthUsage;
    public int duration;

    public ConnectedUserInfo() {}
    public ConnectedUserInfo(String username, int connectionId, int bandwidthUsage, int duration) {
        this.username = username;
        this.connectionId = connectionId;
        this.bandwidthUsage = bandwidthUsage;
        this.duration = duration;
    }

    public String getUsername() { return username; }
    public int getBandwidthUsage() { return bandwidthUsage; }
    public int getDuration() { return duration; }
}

package com.example.tmobilenetworkdemo.Model;

public class ConnectedUserInfo {
    private String username;
    private int connectionId;
    private double bandwidthUsage;
    private int duration;

    public ConnectedUserInfo() {}
    public ConnectedUserInfo(String username, int connectionId, double bandwidthUsage, int duration) {
        this.username = username;
        this.connectionId = connectionId;
        this.bandwidthUsage = bandwidthUsage;
        this.duration = duration;
    }

    public String getUsername() { return username; }
    public double getBandwidthUsage() { return bandwidthUsage; }
    public int getDuration() { return duration; }
    public int getConnectionId() { return connectionId; }
}

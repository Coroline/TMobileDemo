package com.example.tmobilenetworkdemo.Model;

public class ConnectDevice {
    private String ip;
    private String mac;
    private User user;

    public ConnectDevice(String ip, String mac) {
        this.ip = ip;
        this.mac = mac;
    }

    public ConnectDevice(String ip, String mac, User user) {
        this.ip = ip;
        this.mac = mac;
        this.user = user;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }
}

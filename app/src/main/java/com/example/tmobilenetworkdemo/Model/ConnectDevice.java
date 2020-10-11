package com.example.tmobilenetworkdemo.Model;

public class ConnectDevice {
    private String ip;
    private String mac;

    public ConnectDevice(String ip, String mac) {
        this.ip = ip;
        this.mac = mac;
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
}

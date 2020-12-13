package com.example.tmobilenetworkdemo.Model;


/**
 * Model to store information of each transaction item for a specific user/client
 */
public class TransactionInfo {
    private String sessionID;
    private String userName;
    private String clientName;
    private double totalBandwidthUsage;
    private double totalCreditTransferred;
    private int totalDuration;
    private String timeStamp;

    public TransactionInfo() {}

    public TransactionInfo(String sessionID, String userName, String clientName, double totalBandwidthUsage, double totalCreditTransferred, int totalDuration, String timeStamp) {
        this.sessionID = sessionID;
        this.userName = userName;
        this.clientName = clientName;
        this.totalBandwidthUsage = totalBandwidthUsage;
        this.totalCreditTransferred = totalCreditTransferred;
        this.totalDuration = totalDuration;
        this.timeStamp = timeStamp;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }
    public String getSessionID() {
        return sessionID;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getUserName() {
        return userName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientName() {
        return clientName;
    }

    public void setTotalBandwidthUsage(double totalBandwidthUsage) {
        this.totalBandwidthUsage = totalBandwidthUsage;
    }

    public double getTotalBandwidthUsage() {
        return totalBandwidthUsage;
    }

    public void setTotalCreditTransferred(double totalCreditTransferred) {
        this.totalCreditTransferred = totalCreditTransferred;
    }

    public double getTotalCreditTransferred() {
        return totalCreditTransferred;
    }

    public void setTotalDuration(int totalDuration) {
        this.totalDuration = totalDuration;
    }

    public int getTotalDuration() {
        return totalDuration;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getTimeStamp() {
        return timeStamp;
    }
}

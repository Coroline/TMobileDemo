package com.example.tmobilenetworkdemo.Model;

public class User {
    private String fullName;
    private String username;

    public User(String username) {
        this.username = username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}

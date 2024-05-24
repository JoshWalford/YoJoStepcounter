package com.example.mystepcounter2.models;

import java.io.Serializable;

public class User {
    String username;
    String email;
    String password;
    int stepCount;
    int activeTime;
    String date;
    Double distance;
    public User(){}

    public User(String username, String email, String password, int stepCount, int activeTime, String date, Double distance) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.stepCount = stepCount;
        this.activeTime = activeTime;
        this.date = date;
        this.distance = distance;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }

    public int getActiveTime() {
        return activeTime;
    }

    public void setActiveTime(int activeTime) {
        this.activeTime = activeTime;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}

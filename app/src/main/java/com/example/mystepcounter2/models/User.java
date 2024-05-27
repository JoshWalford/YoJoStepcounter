package com.example.mystepcounter2.models;

import java.io.Serializable;

public class User {
    int stepCount;
    int activeTime;
    int height;
    int weight;
    String username;
    String email;
    String password;
    String date;
    String gender;
    Double distance;

    public User() {
    }

    public User(int stepCount, int activeTime, int height, int weight, String username, String email, String password, String date, String gender, Double distance) {
        this.stepCount = stepCount;
        this.activeTime = activeTime;
        this.height = height;
        this.weight = weight;
        this.username = username;
        this.email = email;
        this.password = password;
        this.date = date;
        this.gender = gender;
        this.distance = distance;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
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

package com.example.fiodorko.delivery;

import android.graphics.Color;
import android.location.Geocoder;

import org.osmdroid.util.GeoPoint;

public class Delivery {
    private String recipient, address, date, phone;
    private int id;
    private GeoPoint location;
    private int color;
    private double duration;
    private double distance;

    public Delivery(String recipient, String address, String date, String phone, int id, GeoPoint location) {
        this.recipient = recipient;
        this.address = address;
        this.date = date;
        this.phone = phone;
        this.id = id;
        this.location = location;
        duration = 0;
        distance = 0;
        color = Color.BLACK;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getAddress() {
        return address;
    }

    public String getDate() {
        return date;
    }

    public String getPhone() {
        return phone;
    }

    public int getId() {
        return id;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setId(int id) {
        this.id = id;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }
}

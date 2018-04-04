package com.example.fiodorko.delivery;

public class Delivery {
    private String recipient, address, date, phone;
    private int id;
    private double lat, lon;

    public Delivery(String recipient, String address, String date, String phone, int id) {
        this.recipient = recipient;
        this.address = address;
        this.date = date;
        this.phone = phone;
        this.id = id;
    }

    public Delivery(String recipient, String address, String date, String phone, int id, double lat, double lon) {
        this.recipient = recipient;
        this.address = address;
        this.date = date;
        this.phone = phone;
        
        this.id = id;
        this.lat = lat;
        this.lon = lon;
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

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}

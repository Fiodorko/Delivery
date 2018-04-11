package com.example.fiodorko.delivery;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

public class Delivery implements Parcelable {
    private String recipient, address, date, phone;
    private int id;
    private GeoPoint location;
    private int color;
    private double duration;
    private double distance;
    private Marker marker;
    private ImageView image;
    private Polyline path;
    private boolean first;


    public Delivery(Parcel in) {
        recipient = in.readString();
        address = in.readString();
        date = in.readString();
        phone = in.readString();
        id = in.readInt();
        color = in.readInt();
        duration = in.readDouble();
        distance = in.readDouble();
        first = in.readInt() != 0;
    }


    public Delivery(String recipient, String address, String date, String phone, int id, GeoPoint location, Context ctx) {
        this.recipient = recipient;
        this.address = address;
        this.date = date;
        this.phone = phone;
        this.id = id;
        this.location = location;
        this.duration = 0;
        this.distance = 0;
        this.color = Color.WHITE;
        this.image = new ImageView(ctx);
        this.image.setImageResource(R.drawable.ic_delivery_package_icon);
        this.first = false;
        image.setColorFilter(getColor(), PorterDuff.Mode.MULTIPLY);
    }

    public static final Creator<Delivery> CREATOR = new Creator<Delivery>() {
        @Override
        public Delivery createFromParcel(Parcel in) {
            return new Delivery(in);
        }

        @Override
        public Delivery[] newArray(int size) {
            return new Delivery[size];
        }
    };

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

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
        this.marker.setPosition(location);
        this.marker.setIcon(image.getDrawable());
        this.image.setColorFilter(getColor(), PorterDuff.Mode.MULTIPLY);
    }

    public void setMarker() {
        this.image.setColorFilter(getColor(), PorterDuff.Mode.MULTIPLY);
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(recipient);
        dest.writeString(address);
        dest.writeString(date);
        dest.writeString(phone);
        dest.writeInt(id);
        dest.writeInt(color);
        dest.writeDouble(duration);
        dest.writeDouble(distance);
        dest.writeInt((first ? 1 : 0));
        dest.writeParcelable(location, flags);

    }

    public Polyline getPath() {
        return path;
    }

    public void setPath(Polyline path) {
        this.path = path;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }
}

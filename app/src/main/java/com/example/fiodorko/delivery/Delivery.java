package com.example.fiodorko.delivery;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;

/**
 * Trieda predstavuje informácie o objednávke
 */
public class Delivery implements Parcelable {
    private ArrayList<Item> content;
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
    private String status;

    private ArrayList<Road> road;


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
        content = in.createTypedArrayList(Item.CREATOR);
        status = in.readString();
    }


    public Delivery(Delivery d, Context ctx, MapView map) {
        this.content = d.content;
        this.recipient = d.recipient;
        this.address = d.address;
        this.date = d.date;
        this.phone = d.phone;
        this.id = d.id;
        this.location = d.location;
        this.color = d.color;
        this.duration = d.duration;
        this.distance = d.distance;
        this.image = new ImageView(ctx);
        this.image.setImageResource(R.drawable.ic_delivery_package_icon);
        this.marker = new Marker(map);
        this.marker.setPosition(location);
        this.marker.setIcon(image.getDrawable());
        this.path = d.path;
        this.first = d.first;
        this.status = d.status;
        this.road = new ArrayList<>();
    }

    public Delivery(GeoPoint start, Road road, int color) {
        this.content = null;
        this.recipient = null;
        this.address = null;
        this.date = null;
        this.phone = null;
        this.id = -1;
        this.location = start;
        this.color = color;
        this.marker = null;
        this.path = null;
        this.first = false;
        this.status = null;
        this.road = new ArrayList<>();
        this.road.add(road);
    }

    public Delivery(String recipient, String address, String date, String phone, int id, GeoPoint location, ArrayList<Item> content, Context ctx) {
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
        this.content = content;
        this.road = new ArrayList<>();
        this.status = "Nedokončená";
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
        this.image.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
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
        dest.writeTypedList(content);
        dest.writeString(status);
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

    public ArrayList<Item> getContent() {
        return content;
    }

    public void setContent(ArrayList<Item> content) {
        this.content = content;
    }

    public Road getRoad() {
        if (road.isEmpty()) return null;
        return road.get(0);
    }

    public void setRoad(Road road) {
        this.road = new ArrayList<>();
        this.road.add(road);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

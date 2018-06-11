package com.example.fiodorko.delivery;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Predstavuje položku objednávky
 */
public class Item implements Parcelable {
    private String id;
    private boolean completed;

    Item(String id, boolean completed) {
        this.id = id;
        this.completed = completed;
    }

    private Item(Parcel in) {
        id = in.readString();
        completed = in.readByte() != 0;
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeByte((byte) (completed ? 1 : 0));
    }
}

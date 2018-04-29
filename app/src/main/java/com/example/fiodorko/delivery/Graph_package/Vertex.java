package com.example.fiodorko.delivery.Graph_package;

import org.osmdroid.util.GeoPoint;

public class Vertex {
    public Vertex(int id, int set, int index, GeoPoint location)
    {
        this.location = location;
        this.index = index;
        this.id = id;
        this.set = set;
    }

    public Vertex(Vertex v)
    {
        this.location = v.location;
        this.index = v.index;
        this.id = v.id;
        this.set = v.set;
    }

    public GeoPoint location;
    public int index;
    public int id;
    public int set;
}

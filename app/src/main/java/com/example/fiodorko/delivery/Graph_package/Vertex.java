package com.example.fiodorko.delivery.Graph_package;

import org.osmdroid.util.GeoPoint;

/**
 * Predstavuje vrchol grafu
 */
public class Vertex {
    Vertex(int id, int set, int index, GeoPoint location) {
        this.location = location;
        this.index = index;
        this.id = id;
        this.set = set;
    }

    Vertex(Vertex v) {
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

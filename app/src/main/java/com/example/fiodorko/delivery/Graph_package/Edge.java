package com.example.fiodorko.delivery.Graph_package;

public class Edge {
    public Edge(Vertex a, Vertex b, double weight)
    {
        this.a = a;
        this.b = b;
        this.weight = weight;
    }

    public Edge(Edge e)
    {
        this.a = new Vertex(e.a);
        this.b = new Vertex(e.b);
        this.weight = e.weight;
    }

    public boolean contains(Vertex v)
    {
        return v == a || v == b;
    }

    public boolean contains(int id)
    {
        return id == a.id || id == b.id;
    }

    public Vertex a;
    public Vertex b;
    public double weight;
    public boolean first = false;
    public boolean ab = false;
    public boolean ba = false;

    public String String()
    {
        return a.index+"<->"+b.index + "("+weight+")";
    }
}

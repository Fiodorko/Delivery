package com.example.fiodorko.delivery.Graph_package;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.fiodorko.delivery.Delivery;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

public class Graph {

    private LinkedList<Edge> minimalSpanningTree;
    private ArrayList<Delivery> deliveries;
    private OSRM_API api = new OSRM_API();
    private double distanceMatrix[][];
    private Handler myHandler;
    private ArrayList<Vertex> vertices;
    private LinkedList<Edge> edges;


    public Graph(ArrayList<Delivery> deliveries, GeoPoint start, Handler myHandler)
    {
        this.deliveries = deliveries;
        this.vertices = new ArrayList<>();
        this.vertices.add(new Vertex(0, 0, 0, start));
        this.myHandler = myHandler;
        this.vertices = new ArrayList<>();
        this.edges = new LinkedList<>();
        this.minimalSpanningTree = null;
        int i = 1;

        vertices.add(new Vertex(0, 0, 0, start));
        for (Delivery delivery : deliveries) {
            vertices.add(new Vertex(delivery.getId(), i, i, delivery.getLocation()));
            i++;
        }
        buildDistanceMatrix();
    }

    public ArrayList<Vertex> cloneVertices(ArrayList<Vertex> vertices)
    {
        ArrayList<Vertex> copy = new ArrayList<>();
        for (Vertex vertex: vertices) {
            copy.add(new Vertex(vertex));
        }
        return copy;
    }

    public LinkedList<Edge> cloneEdges(LinkedList<Edge> edges)
    {
        LinkedList<Edge> copy = new LinkedList<>();
        for (Edge edge: edges) {
            copy.add(new Edge(edge));
        }
        return copy;
    }

    private void buildDistanceMatrix() {

        String points = "";

        for (Vertex point : vertices) {
            points = points.concat(String.valueOf(point.location.getLongitude()));
            points = points.concat(",");
            points = points.concat(String.valueOf(point.location.getLatitude()));
            points = points.concat(";");
        }

        points = points.substring(0, points.length() - 2);

        try {
            api.execute(points).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        api.setOnResponseListener(new ResponseListener() {
            @Override
            public void onResponseReceive(double[][] data) {
                if (data.length == 0) {
                    Log.d("JSON", "Matica je prazdna");
                    Message msg = myHandler.obtainMessage(25);
                    myHandler.sendMessage(msg);
                } else {
                    distanceMatrix = data;

                    for (int i = 0; i < data.length; i++) {

                        for (int j = 0; j < data.length; j++) {
                            Log.d(TAG, i+"<->"+j+": "+ data[i][j]);
                        }
                    }


                    buildEdges();
                    Log.d(TAG, "Graf je pripraveny");
                    Message msg = myHandler.obtainMessage(5);
                    myHandler.sendMessage(msg);
                }
            }
        });
    }

    private void buildEdges() {
        for (int j = 0; j < vertices.size(); j++) {
            for (int k = j+1; k < vertices.size(); k++) {
                edges.add(new Edge(vertices.get(j), vertices.get(k), distanceMatrix[j][k]));
            }
        }
    }

    public void removeVertex(int id)
    {
        Vertex removed = null;

        for (Vertex vertex: vertices)
        {
            if(vertex.id == id)
            {
                removed = vertex;
            }
        }

        for (int i = 0; i < edges.size(); i++) {
            if(edges.get(i).contains(removed)) {
                edges.remove(i);
                i--;
            }
        }

        vertices.remove(removed);
    }


    public double[][] getDistanceMatrix() {
        return distanceMatrix;
    }

    private String TAG = "GraphLog";

    public ArrayList<Vertex> getVertices() {
        return vertices;
    }

    public void setDistanceMatrix(double[][] distanceMatrix) {
        this.distanceMatrix = distanceMatrix;
    }

    public void setVertices(ArrayList<Vertex> vertices) {
        this.vertices = vertices;

    }

    public ArrayList<Delivery> getDeliveries() {
        return deliveries;
    }


    public void setEdges(LinkedList<Edge> edges) {
        this.edges = edges;
    }

    public LinkedList<Edge> getEdges() {
        return edges;
    }

    public Edge getEdge(Vertex a, Vertex b)
    {
        for (Edge edge: edges) {
            if(edge.contains(a) && edge.contains(b)) return edge;
        }
        return null;
    }

    public Vertex getStart()
    {
        return vertices.get(0);
    }
}

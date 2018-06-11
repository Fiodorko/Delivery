package com.example.fiodorko.delivery.Graph_package;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.fiodorko.delivery.Delivery;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

/**
 * Predstavuje grafovú podobu adries objednávok, obsahuje cesty medzi všetkými adresami
 */
public class Graph {

    private ArrayList<Delivery> deliveries;
    private OSRM_API api = new OSRM_API();
    private double distanceMatrix[][];
    private Handler myHandler;
    private ArrayList<Vertex> vertices;
    private LinkedList<Edge> edges;

    private LinkedList<Edge> spannningTree = new LinkedList<>();


    /**
     * Prevezme zoznam zásielok a vytvorí podľa ich adries vrcholy grafu
     *
     * @param deliveries - zoznam zásielok
     * @param start      - začiatočný vrchol
     * @param myHandler  - handler eventov (Ak server vráti neplatnú odpoveď, informuje hlavnú triedu)
     */
    public Graph(ArrayList<Delivery> deliveries, GeoPoint start, Handler myHandler) {
        this.deliveries = deliveries;
        this.vertices = new ArrayList<>();
        this.vertices.add(new Vertex(0, 0, 0, start));
        this.myHandler = myHandler;
        this.vertices = new ArrayList<>();
        this.edges = new LinkedList<>();
        int i = 1;

        vertices.add(new Vertex(0, 0, 0, start));
        for (Delivery delivery : deliveries) {
            vertices.add(new Vertex(delivery.getId(), i, i, delivery.getLocation()));
            i++;
        }
        buildDistanceMatrix();
    }

    /**
     * Zostrojí reťazec geolokačných údajov ktoré pošle triede OSRM API aby ich v odoslala webovému API v správnej forme
     */
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
                                Log.d(TAG, i + "<->" + j + ": " + data[i][j]);
                            }
                        }


                        buildEdges();
                        Log.d(TAG, "Graf je pripraveny");
                        Message msg = myHandler.obtainMessage(5);
                        myHandler.sendMessage(msg);
                    }
                }
            });
            api.execute(points).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }


    }

    private void buildEdges() {
        for (int j = 0; j < vertices.size(); j++) {
            for (int k = j + 1; k < vertices.size(); k++) {
                edges.add(new Edge(vertices.get(j), vertices.get(k), distanceMatrix[j][k]));
            }
        }
    }

    public void removeVertex(int id) {
        Vertex removed = null;

        for (Vertex vertex : vertices) {
            if (vertex.id == id) {
                removed = vertex;
            }
        }

        for (int i = 0; i < edges.size(); i++) {
            if (edges.get(i).contains(removed)) {
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

    public ArrayList<Delivery> getDeliveries() {
        return deliveries;
    }


    public LinkedList<Edge> getEdges() {
        return edges;
    }

    public Edge getEdge(Vertex a, Vertex b) {
        for (Edge edge : edges) {
            if (edge.contains(a) && edge.contains(b)) return edge;
        }
        return null;
    }

    public Vertex getStart() {
        return vertices.get(0);
    }

    public void setSpannningTree(LinkedList<Edge> spannningTree) {
        this.spannningTree = spannningTree;
    }

    public LinkedList<Edge> getSpannningTree() {
        return spannningTree;
    }
}

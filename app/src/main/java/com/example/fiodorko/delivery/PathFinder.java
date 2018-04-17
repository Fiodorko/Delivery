package com.example.fiodorko.delivery;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

public class PathFinder {
    private ArrayList<GeoPoint> waypoints;
    private OSRM_API api = new OSRM_API();
    private ArrayList<Delivery> deliveries;
    private GeoPoint start;
    private double distanceMatrix[][];
    private String TAG = "PathFinder";
    private Context ctx;
    private Handler myHandler;
    private ArrayList<Vertex> vertices;
    private LinkedList<Edge> edges;

    private final int[] colors = {hex2Rgb("#3478e5"), hex2Rgb("#41f488"), hex2Rgb("#f44141"), hex2Rgb("#8841f4"), hex2Rgb("#f47f41"), hex2Rgb("#d3f441"), hex2Rgb("#4ff441")};


    private class Edge  {
        Edge(Vertex a, Vertex b, double weight) {
            this.a = a;
            this.b = b;
            this.weight = weight;

        }

        public Vertex a;
        Vertex b;
        double weight;
        boolean first = false;
        boolean ab = false;
        boolean ba = false;


    }

    private class Vertex {
        Vertex(int id, int set, int index, GeoPoint location) {
            this.location = location;
            this.index = index;
            this.id = id;
            this.set = set;
        }

        GeoPoint location;
        int index;
        public int id;
        int set;
    }

    public PathFinder(ArrayList<Delivery> deliveries, GeoPoint start, Context ctx, Handler myHandler) {
        this.start = start;
        this.deliveries = deliveries;
        this.ctx = ctx;
        this.myHandler = myHandler;
        this.vertices = new ArrayList<>();
        this.edges = new LinkedList<>();


        vertices.add(new Vertex(0, 0, 0, start));
        int i = 1;
        for (Delivery delivery : deliveries) {
            vertices.add(new Vertex(delivery.getId(), i, i, delivery.getLocation()));
            i++;
        }

        getDistanceMatrix();

    }

    private void getEdges() {
        for (int j = 0; j < vertices.size(); j++) {
            for (int k = j+1; k < vertices.size(); k++) {
                edges.add(new Edge(vertices.get(j), vertices.get(k), distanceMatrix[j][k]));
            }
        }
    }


    private void getDistanceMatrix() {

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
                if (data == null) Log.d("JSON", "Matica je prazdna");
                distanceMatrix = data;
                getEdges();

                Message msg = myHandler.obtainMessage(5);
                myHandler.sendMessage(msg);
            }
        });
    }


    public ArrayList<Delivery> greedy() {
        ArrayList<Vertex> bestPath = new ArrayList<>();
        ArrayList<Vertex> tmp_vertices = new ArrayList<>(vertices);
        ArrayList<Delivery> tmp_deliveries = new ArrayList<>();

        Vertex actual = tmp_vertices.remove(0);
        bestPath.add(actual);


        while (bestPath.size() < vertices.size()) {
            Vertex next = null;
            double min = Double.MAX_VALUE;
            for (Vertex vertex : tmp_vertices) {
                if (distanceMatrix[actual.index][vertex.index] < min) {
                    next = vertex;
                    min = distanceMatrix[actual.index][vertex.index];
                }
            }

            bestPath.add(tmp_vertices.remove(tmp_vertices.indexOf(next)));
            tmp_deliveries.add(this.deliveries.get(next.index - 1));
            actual = next;
        }

        return tmp_deliveries;
    }


    public ArrayList<Delivery> bestPath(String algorithm) {
        RoadManager roadManager = new OSRMRoadManager(ctx);

        ArrayList<Delivery> tmp_deliiveries = null;

        if (algorithm.equals("Double Spanning Tree")) tmp_deliiveries = doubleSpanningTree(spanningTree(), vertices.get(0));
        else if (algorithm.equals("Greedy")) tmp_deliiveries = greedy();


        Log.d(TAG, tmp_deliiveries.size() + " First: " + tmp_deliiveries.get(0).getAddress());
        ArrayList<GeoPoint> pair = new ArrayList<>(2);

        pair.add(null);
        pair.add(null);

        Polyline roadOverlay;

        for (int i = 0; i < tmp_deliiveries.size(); i++) {

            if (i == 0) {
                pair.set(0, start);
                pair.set(1, tmp_deliiveries.get(i).getLocation());

            } else {
                pair.set(0, tmp_deliiveries.get(i - 1).getLocation());
                pair.set(1, tmp_deliiveries.get(i).getLocation());

            }
            Road road = roadManager.getRoad(pair);

            roadOverlay = RoadManager.buildRoadOverlay(road);
            roadOverlay.setColor(colors[(i) % 6]);
            tmp_deliiveries.get(i).setColor(colors[(i) % 6]);
            tmp_deliiveries.get(i).setDistance(road.mLength);
            tmp_deliiveries.get(i).setDuration(road.mDuration);
            tmp_deliiveries.get(i).setPath(roadOverlay);
            tmp_deliiveries.get(i).setMarker();
        }

        return tmp_deliiveries;

    }

    private static int hex2Rgb(String colorStr) {
        return Color.argb(
                192,
                Integer.valueOf(colorStr.substring(1, 3), 16),
                Integer.valueOf(colorStr.substring(3, 5), 16),
                Integer.valueOf(colorStr.substring(5, 7), 16));
    }


    //Kruskal minimum spanningTree
    private LinkedList<Edge> spanningTree() {
        ArrayList<Vertex> vertices = new ArrayList<>(this.vertices);
        LinkedList<Edge> edges = sortedEdges(this.edges);
        LinkedList<Edge> sequence = new LinkedList<>();

        while(!edges.isEmpty())
        {
            Edge tmp_edge = edges.remove(0);

            int set_a = vertices.get(vertices.indexOf(tmp_edge.a)).set;
            int set_b = vertices.get(vertices.indexOf(tmp_edge.b)).set;

            if(set_a != set_b)
            {
                sequence.add(tmp_edge);
                for (Vertex vertex : vertices) {
                    if(vertex.set == set_b){
                        vertex.set = set_a;
                    }
                }
            }
        }
        return sequence;
    }


    //Merge sort
    private LinkedList<Edge> sortedEdges(LinkedList<Edge> unsorted) {
        LinkedList<Edge> sorted = new LinkedList<>();
        LinkedList<LinkedList<Edge>> sublists = new LinkedList<>();
        sublists.add(unsorted);

        while (sublists.size() != edges.size()) {

            LinkedList<Edge> tmp = sublists.remove(0);
            if (tmp.size() == 1) {
                sublists.add(tmp);
            } else {
                sublists.add(new LinkedList<Edge>(tmp.subList(0, tmp.size() / 2)));
                sublists.add(new LinkedList<Edge>(tmp.subList(tmp.size() / 2, tmp.size())));
            }

        }

        while (sublists.size() != 1) {
            LinkedList<Edge> tmp_1 = sublists.remove(0);
            LinkedList<Edge> tmp_2 = sublists.remove(0);
            sublists.add(merge(tmp_1, tmp_2));
        }

        sorted = sublists.get(0);
        return sorted;
    }

    private LinkedList<Edge> merge(LinkedList<Edge> tmp_1, LinkedList<Edge> tmp_2) {
        LinkedList<Edge> merged = new LinkedList<Edge>();
        while (tmp_1.size() > 0 || tmp_2.size() > 0) {
            if (tmp_1.size() == 0) merged.add(tmp_2.remove(0));
            else if (tmp_2.size() == 0) merged.add(tmp_1.remove(0));
            else if (tmp_1.get(0).weight < tmp_2.get(0).weight) {
                merged.add(tmp_1.remove(0));
            } else
                {
                    merged.add(tmp_2.remove(0));
                }
        }
        return merged;
    }

    private ArrayList<Delivery> doubleSpanningTree(LinkedList<Edge> mst, Vertex start)
    {
        LinkedList<Edge> doubleSpanningTree = new LinkedList<>();

        ArrayList<Vertex> vertices = new ArrayList<>();

        int vertex = start.index;
        vertices.add(start);

        while(doubleSpanningTree.size() < mst.size()*2)
        {
            boolean selected = false;
            for (Edge edge: mst) {
                if(edge.a.index == vertex)
                {
                    if(!edge.first && !edge.ab)
                    {
                        doubleSpanningTree.add(edge);
                        if(!vertices.contains(edge.b)) edge.first = true;
                        edge.ab = true;
                        vertex = edge.b.index;
                        selected = true;
                        break;
                    }
                }
                if(edge.b.index == vertex)
                {
                    if(!edge.first && !edge.ba)
                    {
                        doubleSpanningTree.add(edge);
                        if(!vertices.contains(edge.a)) edge.first = true;
                        edge.ba = true;
                        vertex = edge.a.index;
                        selected = true;
                        break;
                    }
                }
            }

            if(!selected){
            for (Edge edge: mst) {
                if(edge.a.index == vertex)
                {
                    if(!edge.ab)
                    {
                        doubleSpanningTree.add(edge);
                        edge.ab = true;
                        vertex = edge.b.index;
                        break;
                    }
                }
                if(edge.b.index == vertex)
                {
                    if(!edge.ba)
                    {
                        doubleSpanningTree.add(edge);
                        edge.ba = true;
                        vertex = edge.a.index;
                        break;
                    }
                }
            }
            }
        }



        ArrayList<Delivery> result = new ArrayList<>();

        for (Edge edge: doubleSpanningTree) {
            Log.d("Hrany", edge.a.index + " " + edge.b.index + ":" + edge.weight);
        }

        for (Edge edge : doubleSpanningTree)
        {
            int index_a = edge.a.index -1;
            int index_b = edge.b.index -1;
            if(index_a >= 0)
            {
                if (!result.contains(deliveries.get(index_a))) result.add(deliveries.get(index_a));
            }
            if(index_b >= 0)
            {
                if (!result.contains(deliveries.get(index_b))) result.add(deliveries.get(index_b));
            }
        }

        for (Delivery delivery : result)
        {
            Log.d("Hrany" , delivery.getId() + "!");
        }

        return result;
    }

}

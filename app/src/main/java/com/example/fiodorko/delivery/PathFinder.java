package com.example.fiodorko.delivery;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;

import com.example.fiodorko.delivery.Graph_package.Algorithms;
import com.example.fiodorko.delivery.Graph_package.Graph;


import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;

public class PathFinder {


    private GeoPoint start;
    private String TAG = "PathFinder";
    private static int TRANSPARENCY = 192;
    private Context ctx;
    private Graph graph;



    private final int[] colors = {
            hex2Rgb("#3478e5"),
            hex2Rgb("#41f488"),
            hex2Rgb("#f44141"),
            hex2Rgb("#8841f4"),
            hex2Rgb("#f47f41"),
            hex2Rgb("#d3f441"),
            hex2Rgb("#4ff441")};

    PathFinder(ArrayList<Delivery> deliveries, GeoPoint start, Context ctx, Handler myHandler) {
        this.start = start;
        this.ctx = ctx;
        this.graph = new Graph(deliveries, start, myHandler);
    }


    public ArrayList<Delivery> bestPath(String algorithm) {

        RoadManager roadManager = new OSRMRoadManager(ctx);
        //roadManager.addRequestOption();

        ArrayList<Delivery> tmp_deliveries;

        switch (algorithm)
        {
            case "Double Spanning Tree":
                tmp_deliveries = Algorithms.doubleSpanningTree(graph);
                break;
            case "Greedy":
                tmp_deliveries = Algorithms.greedy(graph);
                break;
            case "Insertion Heuristic":
                tmp_deliveries = Algorithms.insertionHeuristic(graph);
                break;
            case "Permutations":
                tmp_deliveries = Algorithms.permutations(graph);
                break;
            default:
                tmp_deliveries = Algorithms.greedy(graph);
                break;
        }

        Log.d(TAG, tmp_deliveries.size() + " First: " + tmp_deliveries.get(0).getAddress());
        ArrayList<GeoPoint> pair = new ArrayList<>(2);

        pair.add(null);
        pair.add(null);

        Polyline roadOverlay;

        for (int i = 0; i < tmp_deliveries.size(); i++) {

            if (i == 0) {
                pair.set(0, start);
                pair.set(1, tmp_deliveries.get(i).getLocation());

            } else {
                pair.set(0, tmp_deliveries.get(i - 1).getLocation());
                pair.set(1, tmp_deliveries.get(i).getLocation());
            }


            Road road = roadManager.getRoad(pair);

            roadOverlay = RoadManager.buildRoadOverlay(road);
            roadOverlay.setColor(colors[(i) % colors.length]);
            tmp_deliveries.get(i).setRoad(road);
            tmp_deliveries.get(i).setColor(colors[(i) % colors.length]);
            //tmp_deliveries.get(i).setMarker(tmp_deliveries.get(i).getColor());
        }

        pair.set(0, pair.get(1));
        pair.set(1, start);
        tmp_deliveries.add(new Delivery(start, ctx, roadManager.getRoad(pair), colors[tmp_deliveries.size() % colors.length]));

        return tmp_deliveries;
    }

    private static int hex2Rgb(String colorStr) {
        return Color.argb(
                TRANSPARENCY,
                Integer.valueOf(colorStr.substring(1, 3), 16),
                Integer.valueOf(colorStr.substring(3, 5), 16),
                Integer.valueOf(colorStr.substring(5, 7), 16));
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public GeoPoint getStart() {
        return start;
    }

    public void setStart(GeoPoint start) {
        this.start = start;
    }
}

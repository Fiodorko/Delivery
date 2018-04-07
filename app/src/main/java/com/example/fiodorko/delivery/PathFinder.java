package com.example.fiodorko.delivery;

import android.content.Context;
import android.util.Log;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class PathFinder {
    private ArrayList<GeoPoint> waypoints;
    private GeoPoint start;
    private double distanceMatrix[][];
    private String TAG = "PathFinder";

    public PathFinder(ArrayList<Delivery> deliveries, GeoPoint start, Context ctx) {
        this.waypoints = new ArrayList<>();
        this.start = start;
        this.waypoints.add(start);

        for (Delivery delivery: deliveries)
        {
            waypoints.add(delivery.getLocation());
        }


        distanceMatrix = new double[waypoints.size()][waypoints.size()];
        RoadManager roadManager = new OSRMRoadManager(ctx);
        ArrayList<GeoPoint> pair = new ArrayList<>(2);

        pair.add(null);
        pair.add(null);

        for (int i = 0; i < waypoints.size(); i++)
        {
            for (int j = 0; j < waypoints.size(); j++)
            {
                pair.set(0,waypoints.get(i));
                pair.set(1,waypoints.get(j));
                distanceMatrix[i][j] = roadManager.getRoad(pair).mLength;
                if(i == 6 || j == 6)Log.d(TAG, waypoints.get(i).getLatitude() + " " + waypoints.get(j).getLatitude());

            }
        }
    }

    public double[][] getDistanceMatrix() {
        for (int i = 0; i < distanceMatrix.length; i++) {
            for (int j = 0; j < distanceMatrix.length; j++) {
                Log.d(TAG, " i:" + i + " j: "+ j + " = " + distanceMatrix[i][j]+ " ");
            }
            Log.d(TAG, "\n");
        }
        return distanceMatrix;
    }




    public ArrayList<GeoPoint> greedy() {
        ArrayList<GeoPoint> bestPath = new ArrayList<>();
        bestPath.add(start);
        int actual = waypoints.indexOf(start);
        while(bestPath.size() < waypoints.size())
        {
            int next = -1;
            double min = Double.MAX_VALUE;
            for (int i = 0; i < waypoints.size(); i++) {
                if(!bestPath.contains(waypoints.get(i)) && actual != i)
                {
                    if(distanceMatrix[actual][i] < min)
                    {
                        min = distanceMatrix[actual][i];
                        next = i;
                    }
                }
            }
            bestPath.add(waypoints.get(next));
            actual = next;
        }
        bestPath.add(start);


        return bestPath;
    }
}

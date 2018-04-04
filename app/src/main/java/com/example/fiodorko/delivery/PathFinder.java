package com.example.fiodorko.delivery;

import android.content.Context;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class PathFinder {
    private ArrayList<GeoPoint> waypoints;
    private GeoPoint start;
    private double distanceMatrix[][];


    public PathFinder(ArrayList<GeoPoint> waypoints, GeoPoint start, Context ctx) {
        this.waypoints = waypoints;
        this.start = start;
        this.waypoints.add(0,start);
        distanceMatrix = new double[waypoints.size()+1][waypoints.size()+1];
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
            }
        }
    }

    public double[][] getDistanceMatrix() {
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
        }
        bestPath.add(start);
        return bestPath;
    }
}

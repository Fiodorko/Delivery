package com.example.fiodorko.delivery;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class PathFinder {
    private ArrayList<GeoPoint> waypoints;
    private OSRM_API api = new OSRM_API();
    private ArrayList<Delivery> deliveries;
    private GeoPoint start;
    private double distanceMatrix[][];
    private String TAG = "PathFinder";
    private Context ctx;

    private final int[] colors = {hex2Rgb("#3478e5"), hex2Rgb("#41f488"), hex2Rgb("#f44141"), hex2Rgb("#8841f4"), hex2Rgb("#f47f41"), hex2Rgb("#d3f441"), hex2Rgb("#4ff441")};


    private class Edge
    {
        public Edge(int a, int b, double weight)
        {
            this.a = a;
            this.b = b;
            this.weight = weight;
        }
        public int a;
        public int b;
        public double weight;
    }

    public PathFinder(ArrayList<Delivery> deliveries, GeoPoint start, Context ctx) {
        this.start = start;
        this.deliveries = deliveries;
        this.ctx = ctx;
    }

    private void getWaypoints()
    {
        waypoints = new ArrayList<>();
        waypoints.add(start);
        for (Delivery delivery: deliveries)
        {
            waypoints.add(delivery.getLocation());
        }
        Log.d(TAG,"Size =" + waypoints.size());
    }

    private void buildDistanceMatrix()
    {
        getWaypoints();
        distanceMatrix = new double[waypoints.size()][waypoints.size()];
        RoadManager roadManager = new OSRMRoadManager(ctx);
        ArrayList<GeoPoint> pair = new ArrayList<>();

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
        getDistanceMatrix();
    }


    public double[][] getDistanceMatrix() {

        String points = "";

        for (GeoPoint point: waypoints) {
            points = points.concat(String.valueOf(point.getLongitude()));
            points = points.concat(",");
            points = points.concat(String.valueOf(point.getLatitude()));
            points = points.concat(";");
        }
        points = points.substring(0, points.length()-2);


        try {
            api.execute(points).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        api.setOnResponseListener(new ResponseListener() {
            @Override
            public void onResponseReceive(double[][] data) {
                Log.d("JSON", "je to tam");
                if(data == null) Log.d("JSON", "Matica je prazdna");
                distanceMatrix = data;
            }
        });





        return distanceMatrix;
    }


    public ArrayList<Delivery> greedy() {
        getWaypoints();
        getDistanceMatrix();

        while(distanceMatrix == null)
        {
            Log.d("x","x");
        }

        Log.d("PathFinder", "cyklus sa ukoncil");
        ArrayList<Delivery> deliveries = new ArrayList<>();
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
            deliveries.add(this.deliveries.get(next-1));
            Log.d("PathFinder", "zasielka c." + this.deliveries.get(next-1).getId() + " pridana");
            actual = next;
        }

        return deliveries;
    }


    public ArrayList<Delivery> greedys() {
        getWaypoints();
        getDistanceMatrix();

        while(distanceMatrix == null)
        {
            Log.d("x","x");
        }

        Log.d("PathFinder", "cyklus sa ukoncil");
        ArrayList<Delivery> deliveries = new ArrayList<>();
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
            deliveries.add(this.deliveries.get(next-1));
            Log.d("PathFinder", "zasielka c." + this.deliveries.get(next-1).getId() + " pridana");
            actual = next;
        }

        return deliveries;
    }


    public ArrayList<Delivery> bestPath(String algorithm)
    {
        RoadManager roadManager = new OSRMRoadManager(ctx);

        if(algorithm.equals("Greedy")) deliveries = greedy();


        Log.d(TAG,deliveries.size() + " First: " + deliveries.get(0).getAddress());
        ArrayList<GeoPoint> pair = new ArrayList<>();

        pair.add(0,start);
        pair.add(1,deliveries.get(0).getLocation());

        Road firstRoad = roadManager.getRoad(pair);

        Polyline roadOverlay = RoadManager.buildRoadOverlay(firstRoad);
        roadOverlay.setColor(colors[0]);
        deliveries.get(0).setColor(colors[0]);
        deliveries.get(0).setDistance(firstRoad.mLength);
        deliveries.get(0).setDuration(firstRoad.mDuration);
        deliveries.get(0).setPath(roadOverlay);
        deliveries.get(0).setMarker();

        for (int i = 1; i < deliveries.size(); i++) {

            pair.set(0, deliveries.get(i-1).getLocation());
            pair.set(1, deliveries.get(i).getLocation());



            Road road = roadManager.getRoad(pair);

            roadOverlay = RoadManager.buildRoadOverlay(road);
            roadOverlay.setColor(colors[(i)%6]);
            deliveries.get(i).setColor(colors[(i)%6]);
            deliveries.get(i).setDistance(road.mLength);
            deliveries.get(i).setDuration(road.mDuration);
            deliveries.get(i).setPath(roadOverlay);
            deliveries.get(i).setMarker();
        }

        return deliveries;

    }

    public static int hex2Rgb(String colorStr) {
        return  Color.argb(
                192,
                Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
                Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
                Integer.valueOf( colorStr.substring( 5, 7 ), 16 ) );
    }

    public void spanningTree()
    {

    }
}

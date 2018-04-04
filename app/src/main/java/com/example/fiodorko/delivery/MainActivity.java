package com.example.fiodorko.delivery;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.Distance;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    MapView map;
    ListView listView;
    private static final String TAG = "Jozo";
    GeoPoint start = new GeoPoint(49.20934716, 18.75783058);

    private double distanceMatrix[][];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);




        Button btn = findViewById(R.id.expandButton);

        ArrayList<Delivery> deliveries = new ArrayList<>();
        ArrayList<GeoPoint> waypoints = new ArrayList<>();



        try {

            InputStream input = getApplicationContext().getAssets().open("deliveries.xml");
            XMLParser parser = new XMLParser(input);

            deliveries = parser.parseXML();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
        e.printStackTrace();
        }


        Geocoder geocoder = new Geocoder(this);

        try {
            for(Delivery delivery : deliveries){
            List<Address> addresses;

            addresses = geocoder.getFromLocationName(delivery.getAddress(),1);
            waypoints.add(new GeoPoint(addresses.get(0).getLatitude(), addresses.get(0).getLongitude()));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        DeliveryListAdapter adapter = new DeliveryListAdapter(this, deliveries);

        listView = (ListView) findViewById(R.id.deliveryListView);

        View header = (View)getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
        listView.addHeaderView(header);

        listView.setAdapter(adapter);




        for(GeoPoint point : waypoints)
        {
            Marker startMarker = new Marker(map);
            startMarker.setPosition(point);
            map.getOverlays().add(startMarker);
        }

        PathFinder pf = new PathFinder(waypoints, start, this);

        RoadManager roadManager = new OSRMRoadManager(this);



//

//        waypoints.add(0,start);
//        waypoints.add(start);

        //Road road = roadManager.getRoad(waypoints);


        int[] colors = {Color.CYAN, Color.GREEN, Color.RED, Color.YELLOW, Color.BLUE, Color.MAGENTA};

        waypoints = pf.greedy();
        ArrayList<GeoPoint> pair = new ArrayList<>();
        pair.add(null);
        pair.add(null);

        Log.d(TAG,waypoints.get(0).getLatitude() + " !");

        for (int i = 0; i < waypoints.size()-1; i++) {
            pair.set(0, waypoints.get(i));
            pair.set(1, waypoints.get(i+1));

            Road road = roadManager.getRoad(pair);

            Polyline roadOverlay = RoadManager.buildRoadOverlay(road);

            roadOverlay.setColor(colors[i%6]);

            map.getOverlays().add(roadOverlay);

            map.invalidate();
        }


        IMapController mapController = map.getController();
        mapController.setZoom(14);
        mapController.setCenter(new GeoPoint(49.221590, 18.741952));


    }

}

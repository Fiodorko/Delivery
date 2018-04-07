package com.example.fiodorko.delivery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Toolbar;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends Activity {



    Intent intent;
    Context ctx;
    Toolbar toolbar;
    private static final String TAG = "MainLog";
    MapView map;
    ListView listView;

    GeoPoint start = new GeoPoint(49.2093471600, 18.7578305800);

    ArrayList<Polyline> bestPath;

    ArrayList<Delivery> deliveries = new ArrayList<>();
    ArrayList<GeoPoint> waypoints = new ArrayList<>();


    int[] colors = {Color.CYAN, Color.GREEN, Color.RED, Color.YELLOW, Color.BLUE, Color.MAGENTA};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ctx = getApplicationContext();



        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeMap();


        findViewById(R.id.fileButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        Log.d(TAG, Environment.getExternalStorageDirectory().getAbsolutePath());
                        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT , Uri.parse("/sdcard/Download/deliveries"));
                        intent.setType("text/xml");

                        startActivityForResult(intent, 1);

            }
        });

        findViewById(R.id.pathButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawPath();

            }
        });

    }

    private void drawPath()
    {
        for(Delivery point : deliveries)
        {
            Marker startMarker = new Marker(map);
            startMarker.setPosition(point.getLocation());
            map.getOverlays().add(startMarker);
        }

        Thread t = new Thread(new Runnable() {
            public void run() {

                bestPath = findBestPath();
            }
        });

        t.start();

        while(t.isAlive())
            {

        }

        for(Polyline path : bestPath)
        {
            map.getOverlays().add(path);
            map.invalidate();
        }
    }

    private ArrayList<Polyline> findBestPath() {
        PathFinder pf = new PathFinder(deliveries, start, this);
        RoadManager roadManager = new OSRMRoadManager(this);
        ArrayList<Polyline> paths = new ArrayList<>();

        waypoints = pf.greedy();
        ArrayList<GeoPoint> pair = new ArrayList<>();
        pair.add(null);
        pair.add(null);

        for (int i = 0; i < waypoints.size()-1; i++) {
            pair.set(0, waypoints.get(i));
            pair.set(1, waypoints.get(i+1));

            Road road = roadManager.getRoad(pair);

            Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
            roadOverlay.setColor(colors[i%6]);
            paths.add(roadOverlay);

        }
        return paths;
    }

    public void initializeMap()
    {
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
        IMapController mapController = map.getController();
        mapController.setZoom(14);
        mapController.setCenter(start);
        map.invalidate();
    }

    public void parseXML(InputStream file){
        Geocoder geocoder = new Geocoder(this);

        try {
            InputStream input = file;
            XMLParser parser = new XMLParser(input, geocoder);
            deliveries = parser.parseXML();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        DeliveryListAdapter adapter = new DeliveryListAdapter(this, deliveries);

        listView = (ListView) findViewById(R.id.deliveryListView);

        listView.setAdapter(adapter);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {

            if (resultCode == RESULT_OK) {
                Uri uri = null;
                if (data != null) {
                    try {
                        parseXML(getContentResolver().openInputStream(data.getData()));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}

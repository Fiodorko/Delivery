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
import android.widget.AdapterView;
import android.widget.ListView;
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

    Thread t = new Thread(new Runnable() {
        public void run() {
            bestPath = findBestPath();
            Message msg = myHandler.obtainMessage(1);
            myHandler.sendMessage(msg);
        }
    });


     Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                try {
                    drawPath();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    int[] colors = {Color.CYAN, Color.GREEN, Color.RED, Color.YELLOW, Color.BLUE, Color.MAGENTA};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ctx = getApplicationContext();



        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.deliveryListView);

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


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parentAdapter, View view,
                                    int position, long id) {

                IMapController mapController = map.getController();
                DeliveryListAdapter adapt = (DeliveryListAdapter) listView.getAdapter();
                mapController.setCenter(adapt.getItem(position).getLocation());
                map.invalidate();
            }
        });
    }

    private void drawPath() throws InterruptedException {

        findViewById(R.id.map).setAlpha((float) 1);
        findViewById(R.id.mapProgress).setVisibility(View.INVISIBLE);

        listView.setAdapter(new DeliveryListAdapter(this, deliveries));

        for(Polyline path : bestPath)
        {

            map.getOverlays().add(path);
            map.invalidate();
        }

        for(Delivery point : deliveries)
        {
            Marker startMarker = new Marker(map);
            startMarker.setPosition(point.getLocation());



            map.getOverlays().add(startMarker);
        }
    }

    private ArrayList<Polyline> findBestPath() {
        PathFinder pf = new PathFinder(deliveries, start, this);
        RoadManager roadManager = new OSRMRoadManager(this);
        ArrayList<Polyline> paths = new ArrayList<>();


        deliveries = pf.greedy();
        Log.d(TAG,deliveries.size() + " First: " + deliveries.get(0).getAddress());
        ArrayList<GeoPoint> pair = new ArrayList<>();

        pair.add(0,start);
        pair.add(1,deliveries.get(0).getLocation());

        Polyline roadOverlay = RoadManager.buildRoadOverlay(roadManager.getRoad(pair));
        roadOverlay.setColor(colors[0]);
        paths.add(roadOverlay);

        for (int i = 0; i < deliveries.size(); i++) {
            Log.d(TAG, deliveries.get(i).getLocation().toDoubleString());
            pair.set(0, deliveries.get(i).getLocation());
            if(i != deliveries.size()-1)
            {
                pair.set(1, deliveries.get(i+1).getLocation());
            } else
                {
                    pair.set(1, start);
                }


            Road road = roadManager.getRoad(pair);

            roadOverlay = RoadManager.buildRoadOverlay(road);
            roadOverlay.setColor(colors[(i+1)%6]);
            deliveries.get(i).setColor(colors[(i)%6]);
            deliveries.get(i).setDistance(road.mLength);
            deliveries.get(i).setDuration(road.mDuration);
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



        listView = (ListView) findViewById(R.id.deliveryListView);

        listView.setAdapter(new DeliveryListAdapter(this, deliveries));

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
                findViewById(R.id.fileButton).setVisibility(View.GONE);


                findViewById(R.id.pathButton).setVisibility(View.VISIBLE);
            }
        }
    }

    public void draw(View v) throws InterruptedException {
        t.start();
        findViewById(R.id.map).setAlpha((float) 0.5);
        findViewById(R.id.mapProgress).setVisibility(View.VISIBLE);
    }


    public void zoom(View v) throws InterruptedException {


    }



}

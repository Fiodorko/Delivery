package com.example.fiodorko.delivery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
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

public class MainActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {


    private int OPTIONS_REQUEST = 2;
    Intent intent;
    Context ctx;
    Toolbar toolbar;
    private static final String TAG = "MainLog";
    MapView map;
    ListView listView;
    Spinner spinner;
    String algorithm;

    GeoPoint start = new GeoPoint(49.2093471600, 18.7578305800);

    ArrayList<Polyline> bestPath;

    ArrayList<Delivery> deliveries = new ArrayList<>();
    ArrayList<GeoPoint> waypoints = new ArrayList<>();

    Thread t = new Thread(new Runnable() {
        public void run() {
            PathFinder pf = new PathFinder(deliveries, start, getBaseContext());
            deliveries = pf.bestPath(algorithm);
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ctx = getApplicationContext();


        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.deliveryListView);

        spinner =  findViewById(R.id.algorithmSelect);
        spinner.setOnItemSelectedListener(this);
        initializeMap();


        findViewById(R.id.fileButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

                if (networkInfo != null && networkInfo.isConnected())
                {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, Uri.parse("/sdcard/Download/deliveries"));
                    intent.setType("text/xml");
                    startActivityForResult(intent, 1);
                } else {
                    Toast.makeText(getApplicationContext(), "Skontrolujte Internetové pripojenie", Toast.LENGTH_SHORT).show();
                }

            }
        });

        listView.setOnItemClickListener(this);

    }

    private void drawPath() throws InterruptedException {

        findViewById(R.id.map).setAlpha((float) 1);
        findViewById(R.id.mapProgress).setVisibility(View.INVISIBLE);

        listView.setAdapter(new DeliveryListAdapter(this, deliveries));

        Log.d("PathFinder", deliveries.size() + " zasielok");

        for (Delivery delivery : deliveries) {
            map.getOverlays().add(delivery.getPath());
            map.invalidate();
        }


    }


    public void initializeMap() {
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
        IMapController mapController = map.getController();
        mapController.setZoom(14);
        mapController.setCenter(start);
        map.invalidate();
    }

    public void parseXML(InputStream file) throws IOException, XmlPullParserException {

        InputStream input = file;
        XMLParser parser = new XMLParser(input, this);
        deliveries = parser.parseXML();

        for (Delivery point : deliveries) {
            Marker marker = new Marker(map);
            point.setMarker(marker);
            map.getOverlays().add(point.getMarker());
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
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    }

                }
                findViewById(R.id.fileButton).setVisibility(View.GONE);
                findViewById(R.id.pathButton).setVisibility(View.VISIBLE);
                findViewById(R.id.algorithmSelect).setVisibility(View.VISIBLE);

                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                        R.array.algorithms, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
            }
        }


        if (requestCode == OPTIONS_REQUEST) {
            if (resultCode == RESULT_OK) {
                int id = -1;
                if (data != null)
                    id = data.getIntExtra("id", 0);
                removeDelivery(id);
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


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        long viewId = view.getId();

        DeliveryListAdapter adapt = (DeliveryListAdapter) listView.getAdapter();
        if (viewId == R.id.detail_button) {

            intent = new Intent(this, DeliveryOptions.class);
            intent.putExtra("parcel_data", adapt.getItem(position));

            startActivityForResult(intent, OPTIONS_REQUEST);
        } else {
            IMapController mapController = map.getController();
            mapController.setCenter(adapt.getItem(position).getLocation());

            if (map.getOverlays().indexOf(adapt.getItem(position).getPath()) != -1) {
                map.getOverlays().remove(adapt.getItem(position).getPath());
                map.getOverlays().add(adapt.getItem(position).getPath());
            }


            map.getOverlays().remove(adapt.getItem(position).getMarker());
            map.getOverlays().add(adapt.getItem(position).getMarker());
            map.invalidate();
        }


    }

    public void removeDelivery(int id) {
        Delivery removed = null;
        for (Delivery delivery : deliveries) {
            if (delivery.getId() == id) {
                map.getOverlays().remove(delivery.getMarker());
                map.getOverlays().remove(delivery.getPath());
                removed = delivery;
            }
        }

        deliveries.remove(removed);
        listView.setAdapter(new DeliveryListAdapter(this, deliveries));
        map.invalidate();

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        algorithm = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        algorithm = getResources().getResourceName(R.string.algorithm);
    }
}

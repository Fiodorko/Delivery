package com.example.fiodorko.delivery;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ListView;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    MapView map;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());


        ArrayList<Delivery> deliveries = new ArrayList<Delivery>();
        try {

            InputStream input = getApplicationContext().getAssets().open("deliveries.xml");
            XMLParser parser = new XMLParser(input);
            deliveries = new ArrayList<Delivery>();
            deliveries = parser.parseXML();
            for(Delivery delivery : deliveries)
            {
                addresses = geocoder.getFromLocationName(delivery.getAddress(), 1);
                delivery.setLat(addresses.get(0).getLatitude());
                delivery.setLon(addresses.get(0).getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
        e.printStackTrace();
        }





        DeliveryListAdapter adapter = new DeliveryListAdapter(this, deliveries);



        listView = (ListView) findViewById(R.id.deliveryListView);

        View header = (View)getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
        listView.addHeaderView(header);

        listView.setAdapter(adapter);



        IMapController mapController = map.getController();
        mapController.setZoom(14);
        mapController.setCenter(new GeoPoint(49.221590, 18.741952));






    }
}

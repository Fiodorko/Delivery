package com.example.fiodorko.delivery;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

    private final int CHOOSE_FILE = 1;
    private final int OPTIONS_REQUEST = 2;
    private final int EXPORT_REQUEST = 42;

    /**
     * Objekt pathfinder má na starosť vytvorenie grafovej podoby objednávok a preberá z OSRM informácie o cestách medzi jednotlivými ojednávkami
     */
    private PathFinder pathFinder = null;
    private static final String TAG = "MainLog";
    private Uri uri;


    TextView totalDuration;
    TextView totalDistance;
    TextView selectedAlgorithm;
    Intent intent;
    Context ctx;

    /**
     * Predstavuje objekt mapy. Je to trieda z knižnice osmdorid
     */
    MapView map = null;
    ListView listView;
    Spinner algorithmSelect;
    String algorithm;
    Spinner menu;

    Button fileButton;
    Button pathButton;
    Marker storageMarker;

    /**
     * Začiatočný bod kuriéra
     */
    GeoPoint start = new GeoPoint(49.2093471600, 18.7578305800);

    /**
     * Zoznam načítaných objednávok
     */
    ArrayList<Delivery> deliveries = new ArrayList<>();
    /**
     * Zoznam dokončených objednávok
     */
    ArrayList<Delivery> finishedDeliveries = new ArrayList<>();
    /**
     * Zoznam objednávok utriedených poľa algoritmu
     */
    ArrayList<Delivery> orderedDeliveries = new ArrayList<>();

    Thread createPathFinder = new Thread(new Runnable() {
        public void run() {
            Log.d(TAG, "Chceking pathfinder...");
            if (pathFinder == null) {
                pathFinder = new PathFinder(copy(deliveries), start, getBaseContext(), myHandler);
                Log.d(TAG, "Pathfinder Created!");
            } else {
                Message msg = myHandler.obtainMessage(5);
                myHandler.sendMessage(msg);
                Log.d(TAG, "Pathfinder already existed!");
            }
        }
    });

    Thread getBestPath = new Thread(new Runnable() {
        public void run() {
            Log.d(TAG, "Obtaining best path...");
            orderedDeliveries = pathFinder.bestPath(algorithm);
            if (orderedDeliveries != null) {
                Log.d(TAG, "Best path aquired!");
                Message msg = myHandler.obtainMessage(10);
                myHandler.sendMessage(msg);
            }
        }
    });

    Thread parseFile = new Thread(new Runnable() {
        public void run() {
            Log.d(TAG, "Parsing XML");
            try {
                parseXML(getContentResolver().openInputStream(Objects.requireNonNull(uri)));

            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
                Log.d(TAG, "Parsing Failed");
                Message msg = myHandler.obtainMessage(50);
                myHandler.sendMessage(msg);
            }
            if (!deliveries.isEmpty()) {
                Message msg = myHandler.obtainMessage(20);
                myHandler.sendMessage(msg);
            }
        }
    });


    @SuppressLint("HandlerLeak")
    Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 5:
                    getBestPath.start();
                    break;
                case 10:
                    drawPath();
                    break;
                case 15:
                    parseFile.start();
                    break;
                case 20:
                    hideFileSelect();
                    break;
                case 25:
                    Toast.makeText(getApplicationContext(), "Príliš veľa požiadaviek skúste to neskôr", Toast.LENGTH_SHORT).show();
                    pathFinder = null;
                    showPathFinder();
                    break;
                case 50:
                    Toast.makeText(getApplicationContext(), "Súbor sa nepodarilo načítať skúste to znova", Toast.LENGTH_SHORT).show();
                    break;
                case 429:
                    Toast.makeText(getApplicationContext(), "Pri získavaní cesty sa vyskytla chyba!", Toast.LENGTH_SHORT).show();
                    showPathFinder();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ctx = getApplicationContext();
        Log.d(TAG, "In the onCreate() event");

        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        View decorView = getWindow().getDecorView();

        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean fileLoaded = false;
        if (savedInstanceState != null) {
            if (fileLoaded) {
                deliveries = savedInstanceState.getParcelableArrayList("deliveries");
            }
        }

        totalDuration = findViewById(R.id.total_duration);
        totalDistance = findViewById(R.id.total_distance);

        listView = findViewById(R.id.deliveryListView);
        listView.setOnItemClickListener(this);
        listView.setAdapter(new DeliveryListAdapter(this, deliveries));

        algorithmSelect = findViewById(R.id.algorithmSelect);
        algorithmSelect.setOnItemSelectedListener(this);

        fileButton = findViewById(R.id.fileButton);
        pathButton = findViewById(R.id.pathButton);
        menu = findViewById(R.id.menu);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(MainActivity.this,
                R.array.menu, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(R.layout.dropdown_item);

        menu.setAdapter(adapter);
        menu.setOnItemSelectedListener(this);

        algorithmSelect.setVisibility(View.VISIBLE);
        finishedDeliveries = new ArrayList<>();
        if (map == null) {
            initializeMap();

        }

        if (savedInstanceState != null) {
            if (fileLoaded) {
                for (Delivery delivery : deliveries) {
                    map.getOverlays().add(RoadManager.buildRoadOverlay(delivery.getRoad()));
                    map.getOverlays().add(delivery.getMarker());
                    map.invalidate();
                }
                hideFileSelect();
            }
        }
    }


    /**
     * Metóda inicializuje mapu, nastaví typ mapy, zoomovacie nastavenia a vycentruje mapu na začiatočný bod ktorému tiež nastaví ikonu skladu.
     */
    private void initializeMap() {
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);

        IMapController mapController = map.getController();
        mapController.setZoom(14.0);
        mapController.setCenter(start);

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_storage_icon);

        this.storageMarker = new Marker(map);
        this.storageMarker.setIcon(icon.getDrawable());
        this.storageMarker.setPosition(start);

        map.getOverlays().add(storageMarker);
        map.invalidate();
    }

    /**
     * Vykreslí cesty všetkých objednávok na mapu
     */
    private void drawPath() {
        clearMap();
        findViewById(R.id.map).setAlpha((float) 1);
        findViewById(R.id.mapProgress).setVisibility(View.INVISIBLE);

        listView.setAdapter(new DeliveryListAdapter(this, orderedDeliveries));

        for (Delivery delivery : orderedDeliveries) {
            Polyline tmp = RoadManager.buildRoadOverlay(delivery.getRoad(),
                    delivery.getColor(),
                    7.5f);

            delivery.setPath(tmp);
            map.getOverlays().add(tmp);
            if (delivery.getId() != -1) map.getOverlays().add(delivery.getMarker());
            map.invalidate();
        }

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_storage_icon);
        icon.setColorFilter(
                deliveries.get(deliveries.size() - 1).getColor(), PorterDuff.Mode.MULTIPLY);
        storageMarker.setIcon(icon.getDrawable());
        map.getOverlays().add(storageMarker);
        hidePathFinder();

    }

    /**
     * Načíta a spracuje súbor file s objednávkami
     *
     * @param file - objekt predstavujúci vzbraný súbor
     * @throws IOException            - výnimka pri čítaní súboru
     * @throws XmlPullParserException - výnimka pri parsovaní súboru
     */
    private void parseXML(InputStream file) throws IOException, XmlPullParserException {
        XMLParser parser = new XMLParser(file, this);
        deliveries = parser.parseXML();

        for (Delivery point : deliveries) {
            Marker marker = new Marker(map);
            point.setMarker(marker);
            map.getOverlays().add(point.getMarker());
        }

    }


    /**
     * Vymaže objednávku zo zoznamu aktuálnych objednávok a zmaže jej ikonu a cestu z mapy.
     *
     * @param id - identifikátor objednávky
     */
    public void removeDelivery(int id) {
        Delivery removed = null;
        for (Delivery delivery : orderedDeliveries) {
            if (delivery.getId() == id) {
                map.getOverlays().remove(delivery.getMarker());
                map.getOverlays().remove(delivery.getPath());
                removed = delivery;
            }
        }

        if (pathFinder != null || removed != null) {
            assert removed != null;
            pathFinder.setStart(removed.getLocation());
            pathFinder.getGraph().removeVertex(removed.getId());
        }

        orderedDeliveries.remove(removed);
        finishedDeliveries.add(removed);

        listView.setAdapter(new DeliveryListAdapter(this, orderedDeliveries));
        map.invalidate();
        refreshRouteInfo();
    }

    /**
     * Zavolá aktivitu na vybranie súboru s objednávkami
     *
     * @param v tlačidlo ktoré spúšťa metódu
     */
    public void chooseFile(View v) {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connMgr != null;
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            @SuppressLint("SdCardPath") Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, Uri.parse("/sdcard/Download/deliveries"));
            intent.setType("text/xml");
            startActivityForResult(intent, CHOOSE_FILE);
        } else {
            Toast.makeText(getApplicationContext(), "Skontrolujte Internetové pripojenie", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * spustí vlákno ktoré vztvorí triedu PathFinder.
     *
     * @param v tlačidlo ktoré spúšťa metódu
     */
    public void draw(View v) {
        createPathFinder.start();
        findViewById(R.id.pathButton).setClickable(false);
        findViewById(R.id.map).setAlpha((float) 0.5);
        findViewById(R.id.mapProgress).setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CHOOSE_FILE:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        uri = data.getData();
                        Message msg = myHandler.obtainMessage(15);
                        myHandler.sendMessage(msg);
                    }

                }
                break;

            case OPTIONS_REQUEST:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        Delivery modified = data.getParcelableExtra("delivery");
                        for (Delivery delivery : deliveries) {

                            if (delivery.getId() == data.getIntExtra("id", 0)) {
                                delivery.setStatus(
                                        modified.getStatus()
                                );
                                delivery.setContent(
                                        modified.getContent()
                                );
                            }
                        }
                        removeDelivery(modified.getId());
                    }


                } else if (resultCode == RESULT_CANCELED) {
                    if (data != null) {
                        Delivery modified = data.getParcelableExtra("delivery");
                        for (Delivery delivery : deliveries) {

                            if (delivery.getId() == data.getIntExtra("id", 0)) {
                                delivery.setStatus(
                                        modified.getStatus()
                                );
                                delivery.setContent(
                                        modified.getContent()
                                );
                            }
                        }
                    }
                }

                break;
            case EXPORT_REQUEST:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        try {

                            OutputStreamWriter stream =
                                    new OutputStreamWriter(
                                            Objects.requireNonNull(
                                                    getContentResolver().openOutputStream(
                                                            Objects.requireNonNull(data.getData()))));
                            stream.write(XMLWriter.writeXml(finishedDeliveries));
                            stream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;

        }
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

            if (adapt.getItem(position).getId() != -1) {
                map.getOverlays().remove(adapt.getItem(position).getMarker());
                map.getOverlays().add(adapt.getItem(position).getMarker());
            }

            map.invalidate();
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        algorithm = parent.getItemAtPosition(position).toString();

        String selected = parent.getItemAtPosition(position).toString();

        Log.d("Menu", parent.getItemAtPosition(position).toString() + " was selected.");

        switch (selected) {
            case "Export":
                if (!finishedDeliveries.isEmpty()) {
                    @SuppressLint("SdCardPath") Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT, Uri.parse("/sdcard/Download/deliveries"));
                    intent.putExtra(Intent.EXTRA_TITLE, "Report" + Calendar.getInstance().getTime());
                    intent.setType("text/xml");
                    startActivityForResult(intent, EXPORT_REQUEST);
                } else {
                    Toast.makeText(getApplicationContext(), "Nie je čo exportovať", Toast.LENGTH_SHORT).show();

                }
                break;
            case "Reset":
                if (!orderedDeliveries.isEmpty()) {
                    reset();
                    parent.setSelection(0);
                } else {
                    Toast.makeText(getApplicationContext(), "Zatiaľ ste nevykonali žiadne zmeny", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }


    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        algorithm = getResources().getResourceName(R.string.algorithm);
    }


    /**
     * Vráti celkovú dobu za ktorú je možné cestu absolvovať
     *
     * @return celková doba
     */
    private double getDuration() {
        double result = 0;
        for (Delivery delivery : orderedDeliveries) {
            result += delivery.getRoad().mDuration;
        }
        return result;
    }

    /**
     * Vráti celkovú dĺžku cesty
     *
     * @return celková dĺžka
     */
    private double getDistance() {
        double result = 0;
        for (Delivery delivery : orderedDeliveries) {
            result += delivery.getRoad().mLength;
        }
        return result;
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void refreshRouteInfo() {
        double time = getDuration();
        int hours = (int) time / 3600;
        time -= hours * 3600;
        int minutes = (int) time / 60;
        time -= minutes * 60;

        totalDuration.setText(
                getString(R.string.totalDuration) + " "
                        + String.format("%02d", hours) + ":"
                        + String.format("%02d", minutes) + ":"
                        + String.format("%02.2f", time) + "");

        totalDistance.setText(getString(R.string.totalDistance) + " " + String.format("%.2f", getDistance()) + "km");
    }

    public ArrayList<Delivery> copy(ArrayList<Delivery> original) {
        ArrayList<Delivery> copy = new ArrayList<>();
        for (Delivery delivery : original) {
            copy.add(new Delivery(delivery, this, map));
        }
        return copy;
    }

    /**
     * Zruší aktuálne nájdenú cestu a vráti aplikáciu do stavu kedy si môže užívateľ zvoliť algoritmus na nájdenie najlepšej cesty
     */
    public void reset() {
        hideRouteInfo();
        clearMap();
        for (Delivery point : deliveries) {
            Marker marker = new Marker(map);
            point.setMarker(marker);
            map.getOverlays().add(point.getMarker());
        }
        orderedDeliveries = new ArrayList<>();
        finishedDeliveries = new ArrayList<>();
        map.invalidate();
        listView.setAdapter(new DeliveryListAdapter(this, deliveries));
    }


    /**
     * Vymaže z mapy všetky overlaye a pridá iba ikonu skladu na začiatočný bod
     */
    public void clearMap() {
        while (!map.getOverlays().isEmpty()) {
            map.getOverlays().remove(0);
        }

        Marker storageMarker = new Marker(map);
        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_storage_icon);
        storageMarker.setIcon(icon.getDrawable());
        storageMarker.setPosition(start);
        map.getOverlays().add(storageMarker);

        map.invalidate();

    }

    private void hideFileSelect() {
        listView.setAdapter(new DeliveryListAdapter(this, deliveries));
        fileButton.setVisibility(View.GONE);
        map.invalidate();
        showPathFinder();
    }

    @SuppressLint("SetTextI18n")
    private void hidePathFinder() {

        pathButton.setVisibility(View.GONE);
        algorithmSelect.setVisibility(View.GONE);

        findViewById(R.id.route_info).setVisibility(View.VISIBLE);

        selectedAlgorithm = findViewById(R.id.selectedAlgorithm);
        selectedAlgorithm.setText(algorithm + getString(R.string.deliveryCount) + deliveries.size());
        refreshRouteInfo();
    }

    private void hideRouteInfo() {
        findViewById(R.id.route_info).setVisibility(View.GONE);
        showPathFinder();
    }

    private void showPathFinder() {
        findViewById(R.id.map).setAlpha((float) 1);
        findViewById(R.id.mapProgress).setVisibility(View.INVISIBLE);
        pathButton.setVisibility(View.VISIBLE);
        findViewById(R.id.pathButton).setClickable(true);
        algorithmSelect.setVisibility(View.VISIBLE);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                MainActivity.this, R.array.algorithms, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(R.layout.dropdown_item);

        algorithmSelect.setAdapter(adapter);
    }


}

package com.example.fiodorko.delivery.Graph_package;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Preberá maticu ohodnotení hrán z OSRM_API
 */
public class OSRM_API extends AsyncTask<String, Void, String> {

    private ResponseListener listener;

    public void setOnResponseListener(ResponseListener listener) {
        this.listener = listener;
    }


    @Override
    protected String doInBackground(String... urls) {
        try {
            Log.d("JSON", "Preberam informaciu z OSRM API");
            return downloadUrl(urls[0]);
        } catch (IOException e) {
            return "Nie je možné stiahnuť informácie z webu!";
        }
    }

    /**
     * Spracuje odpoveď OSRM API a z JSON objektu vztvorí dvojrozmerné pole typu double
     *
     * @param result odpoveď OSRM api v tvare JSON objektu
     */
    protected void onPostExecute(String result) {
        if (result.equals("Nie je možné stiahnuť informácie z webu!")) {
            Log.d("OSRM", "Nie je možné stiahnuť informácie z webu!");
            listener.onResponseReceive(new double[0][0]);
            return;
        }
        double[][] matrix;
        try {
            JSONObject jsonResponse = new JSONObject(result);
            JSONArray array = jsonResponse.getJSONArray("durations");
            matrix = new double[array.length()][array.length()];
            for (int i = 0; i < array.length(); i++) {
                for (int j = 0; j < array.length(); j++) {
                    matrix[i][j] = array.getJSONArray(i).getDouble(j);
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);

        }
        listener.onResponseReceive(matrix);
    }

    private String downloadUrl(String points) throws IOException {
        InputStream is = null;

        if (points.equals("Nie je možné stiahnuť informácie z webu!")) return points;

        try {
            URL url = new URL("http://185.33.146.213:5000/table/v1/driving/" + points);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();

            is = conn.getInputStream();

            return convertStreamToString(is);

        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

}

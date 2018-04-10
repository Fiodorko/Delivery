package com.example.fiodorko.delivery;

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

public class OSRM_API extends AsyncTask<String, Void, String> {

    ResponseListener listener;
    public void setOnResponseListener(ResponseListener listener) {
        this.listener = listener;
    }


    @Override
    protected String doInBackground(String... urls) {
        try {
            Log.d("JSON", "robim");
            return downloadUrl(urls[0]);
        } catch (IOException e) {
            return "Nie je možné stiahnuť informácie z webu! Chybná URL?";
        }
    }

    protected void onPostExecute(String result) {

        double[][] matrix = null;

        try {
            JSONObject jsonResponse = new JSONObject(result);

            JSONArray array = jsonResponse.getJSONArray("durations");
            Log.d("JSON" , array.toString());
            matrix = new double[array.length()][array.length()];
            for (int i = 0; i < array.length(); i++)
            {
                for (int j = 0; j < array.length(); j++)
                {
                    matrix[i][j] = array.getJSONArray(i).getDouble(j);
                }
            }
        } catch (JSONException e) {

    }
        listener.onResponseReceive(matrix);
    }

    private String downloadUrl(String points) throws IOException {
        InputStream is = null;

        try {
            URL url = new URL("http://router.project-osrm.org/table/v1/driving/" + points);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            conn.connect();
            int response = conn.getResponseCode();

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

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
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

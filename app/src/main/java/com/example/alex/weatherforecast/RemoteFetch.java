package com.example.alex.weatherforecast;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class RemoteFetch {

    private static final String TAG = RemoteFetch.class.getSimpleName();
    private static final String APPID = "68ecb9f534e9aa87f423b522070def30";
    private static final String OPEN_WEATHER_MAP_API =
            "http://api.openweathermap.org/data/2.5/forecast/daily?lat=%f&lon=%f&units=metric&cnt=5&APPID=%s";
    private static final String COD = "cod";

    public static JSONObject getJSON(final double lat, final double lon) {
        try {
            URL url = new URL(String.format(OPEN_WEATHER_MAP_API, lat, lon, APPID));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder json = new StringBuilder(1024);
            String tmp;

            while ((tmp = reader.readLine()) != null) {
                json.append(tmp).append("\n");
            }
            reader.close();

            JSONObject data = new JSONObject(json.toString());

            if (data.getInt(COD) != 200) {
                return null;
            }

            return data;
        } catch (MalformedURLException e) {
            Log.e(TAG, "Unable to create url", e);
        } catch (IOException e) {
            Log.e(TAG, "Unable to open connection", e);
        } catch (JSONException e) {
            Log.e(TAG, "Unable to create json", e);
        }
        return null;
    }
}

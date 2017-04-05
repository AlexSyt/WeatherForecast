package com.example.alex.weatherforecast;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();
    private TextView tvLocation;
    private List<Forecast> forecasts;
    private CardAdapter adapter;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLocation = (TextView) findViewById(R.id.location_tv);
        forecasts = new ArrayList<>();
        handler = new Handler();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        String location = "kyiv,ua";
        updateWeather(location);
        tvLocation.setText(location);

        adapter = new CardAdapter(forecasts, this);
        recyclerView.setAdapter(adapter);
    }

    //todo: get city from current location
    //todo: add menu for update
    //todo: cache weather
    private void updateWeather(final String city) {
        new Thread() {
            @Override
            public void run() {
                final JSONObject json = RemoteFetch.getJSON(city);
                if (json != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            renderWeather(json);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        }.start();
    }

    private void renderWeather(JSONObject json) {
        try {
            JSONArray jsonArray = json.getJSONArray("list");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jDayForecast = jsonArray.getJSONObject(i);
                JSONObject temp = jDayForecast.getJSONObject("temp");
                JSONObject weather = jDayForecast.getJSONArray("weather").getJSONObject(0);
                Forecast forecast = new Forecast();

                forecast.setDescription(weather.getString("description"));
                forecast.setAverageDay(temp.getDouble("day"));
                forecast.setAverageNight(temp.getDouble("night"));
                forecast.setWind(jDayForecast.getDouble("speed"));
                forecast.setPressure(jDayForecast.getDouble("pressure"));
                forecast.setHumidity(jDayForecast.getDouble("humidity"));
                forecast.setDate(new Date(jDayForecast.getLong("dt") * 1000));
                forecast.setIconName(weather.getString("icon"));

                forecasts.add(forecast);
                Log.i(TAG, "create forecast" + i);
            }
        } catch (JSONException e) {
            Log.e(TAG, "One or more fields not found in the JSON data", e);
        }
    }
}

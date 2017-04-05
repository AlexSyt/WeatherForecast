package com.example.alex.weatherforecast;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener {

    private final String TAG = MainActivity.class.getSimpleName();
    private TextView tvLocation;
    private List<Forecast> forecasts;
    private CardAdapter adapter;
    private Handler handler;
    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLocation = (TextView) findViewById(R.id.location_tv);
        forecasts = new ArrayList<>();
        handler = new Handler();

        googleApiClient = new GoogleApiClient.Builder(this, this, this).addApi(LocationServices.API).build();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new CardAdapter(forecasts, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_update:
                forecasts.clear();
                onConnected(null);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            updateWeather(lastLocation.getLatitude(), lastLocation.getLongitude());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //todo: add menu for update
    //todo: cache weather

    private void updateWeather(final double lat, final double lon) {
        new Thread() {
            @Override
            public void run() {
                final JSONObject json = RemoteFetch.getJSON(lat, lon);
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
            tvLocation.setText(json.getJSONObject("city").getString("name"));

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
            }
        } catch (JSONException e) {
            Log.e(TAG, "One or more fields not found in the JSON data", e);
        }
    }
}

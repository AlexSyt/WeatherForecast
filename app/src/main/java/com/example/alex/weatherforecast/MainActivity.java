package com.example.alex.weatherforecast;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
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
import java.util.Calendar;
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
                googleApiClient.disconnect();
                googleApiClient.connect();
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
            if (lastLocation != null) {
                updateWeather(lastLocation.getLatitude(), lastLocation.getLongitude(), this);
            } else {
                updateWeather(-1, -1, this);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void updateWeather(final double lat, final double lon, final Context context) {
        new Thread() {
            @Override
            public void run() {
                final JSONObject[] json = new JSONObject[1];
                if (lat == -1 && lon == -1) {
                    try {
                        json[0] = new JSONObject(PreferenceManager.
                                getDefaultSharedPreferences(context).getString("theJson", ""));
                    } catch (JSONException e) {
                        Log.e(TAG, "Unable to create json", e);
                    }
                } else {
                    json[0] = RemoteFetch.getJSON(lat, lon);
                    if (json[0] != null) {
                        PreferenceManager.getDefaultSharedPreferences(context).edit()
                                .putString("theJson", json[0].toString()).apply();
                    }
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        renderWeather(json[0]);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }.start();
    }

    private void renderWeather(JSONObject json) {
        try {
            JSONArray jsonArray = json.getJSONArray("list");
            tvLocation.setText(json.getJSONObject("city").getString("name"));

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jDayForecast = jsonArray.getJSONObject(i);

                Date forecastDate = new Date(jDayForecast.getLong("dt") * 1000);
                Date now = new Date();
                Calendar forecastCalendar = Calendar.getInstance();
                Calendar nowCalendar = Calendar.getInstance();

                forecastCalendar.setTime(forecastDate);
                forecastCalendar.set(Calendar.HOUR_OF_DAY, 0);
                forecastCalendar.set(Calendar.MINUTE, 0);
                forecastCalendar.set(Calendar.SECOND, 0);
                forecastCalendar.set(Calendar.MILLISECOND, 0);

                nowCalendar.setTime(now);
                nowCalendar.set(Calendar.HOUR_OF_DAY, 0);
                nowCalendar.set(Calendar.MINUTE, 0);
                nowCalendar.set(Calendar.SECOND, 0);
                nowCalendar.set(Calendar.MILLISECOND, 0);

                int compare = forecastCalendar.compareTo(nowCalendar);
                if (compare == 0 || compare == 1) {
                    JSONObject temp = jDayForecast.getJSONObject("temp");
                    JSONObject weather = jDayForecast.getJSONArray("weather").getJSONObject(0);
                    Forecast forecast = new Forecast();

                    forecast.setDescription(weather.getString("description"));
                    forecast.setAverageDay(temp.getDouble("day"));
                    forecast.setAverageNight(temp.getDouble("night"));
                    forecast.setWind(jDayForecast.getDouble("speed"));
                    forecast.setPressure(jDayForecast.getDouble("pressure"));
                    forecast.setHumidity(jDayForecast.getDouble("humidity"));
                    forecast.setDate(forecastDate);
                    forecast.setIconName(weather.getString("icon"));

                    forecasts.add(forecast);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "One or more fields not found in the JSON data", e);
        }
    }
}

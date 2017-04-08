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
import android.widget.Toast;

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
    private static final String JSON_KEY = "theJson";
    private static final String UNABLE_TO_LOAD_FORECAST = "Unable to load forecast. " +
            "Check the Internet and Location and try to reload the forecast.";
    private static final String LIST = "list";
    private static final String CITY = "city";
    private static final String NAME = "name";
    private static final String DATE = "dt";
    private static final String TEMP = "temp";
    private static final String WEATHER = "weather";
    private static final String DESCRIPTION = "description";
    private static final String DAY = "day";
    private static final String NIGHT = "night";
    private static final String SPEED = "speed";
    private static final String PRESSURE = "pressure";
    private static final String HUMIDITY = "humidity";
    private static final String ICON = "icon";
    private static final int wrongLat = -1;
    private static final int wrongLon = -1;
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
                updateWeather(wrongLat, wrongLon, this);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * Creates a new thread for receiving and processing of json-object with forecast.
     * If it was not possible to get the current latitude and longitude - we are trying to load
     * the cached version of forecast. Otherwise we load the forecast from the Internet.
     * If the forecast was loaded correctly - we process and display it. Otherwise, we inform
     * the user about the problem.
     *
     * @param lat     latitude of current location.
     * @param lon     longitude of current location.
     * @param context current Context.
     */
    private void updateWeather(final double lat, final double lon, final Context context) {
        new Thread() {
            @Override
            public void run() {
                final JSONObject[] json = new JSONObject[1];
                if (lat == wrongLat && lon == wrongLon) {
                    try {
                        json[0] = new JSONObject(PreferenceManager.
                                getDefaultSharedPreferences(context).getString(JSON_KEY, ""));
                    } catch (JSONException e) {
                        Log.e(TAG, "Unable to load json from cache", e);
                    }
                } else {
                    json[0] = RemoteFetch.getJSON(lat, lon);
                    if (json[0] != null) {
                        PreferenceManager.getDefaultSharedPreferences(context).edit()
                                .putString(JSON_KEY, json[0].toString()).apply();
                    }
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            renderWeather(json[0]);
                            adapter.notifyDataSetChanged();
                        } catch (NullPointerException e) {
                            Toast.makeText(MainActivity.this, UNABLE_TO_LOAD_FORECAST,
                                    Toast.LENGTH_LONG).show();
                            Log.e(TAG, "json with forecast == null", e);
                        }
                    }
                });
            }
        }.start();
    }

    /**
     * Converts the json-object to objects of Forecast class.
     *
     * @param json json-object with forecast.
     */
    private void renderWeather(JSONObject json) {
        try {
            JSONArray jsonArray = json.getJSONArray(LIST);
            tvLocation.setText(json.getJSONObject(CITY).getString(NAME));

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jDayForecast = jsonArray.getJSONObject(i);

                Date forecastDate = new Date(jDayForecast.getLong(DATE) * 1000);
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
                    JSONObject temp = jDayForecast.getJSONObject(TEMP);
                    JSONObject weather = jDayForecast.getJSONArray(WEATHER).getJSONObject(0);
                    Forecast forecast = new Forecast();

                    forecast.setDescription(weather.getString(DESCRIPTION));
                    forecast.setAverageDay(temp.getDouble(DAY));
                    forecast.setAverageNight(temp.getDouble(NIGHT));
                    forecast.setWind(jDayForecast.getDouble(SPEED));
                    forecast.setPressure(jDayForecast.getDouble(PRESSURE));
                    forecast.setHumidity(jDayForecast.getDouble(HUMIDITY));
                    forecast.setDate(forecastDate);
                    forecast.setIconName(weather.getString(ICON));

                    forecasts.add(forecast);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "One or more fields not found in the JSON data", e);
        }
    }
}

package com.example.alex.weatherforecast;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private final static String DATE_FORMAT = "dd.MM.yyyy";
    private final static String AVERAGE_DAY_LAYOUT = "Average Day: %.2f ℃";
    private final static String AVERAGE_NIGHT_LAYOUT = "Average Night: %.2f ℃";
    private final static String WIND_LAYOUT = "Wind: %.2f km/h";
    private final static String PRESSURE_LAYOUT = "Pressure: %.2f hPa";
    private final static String HUMIDITY_LAYOUT = "Humidity: %.2f%%";
    private final static String ICON_PATH = "http://openweathermap.org/img/w/";
    private final static String ICON_EXTENSION = ".png";
    private final List<Forecast> forecasts;
    private final Context context;

    public CardAdapter(List<Forecast> forecasts, Context context) {
        this.forecasts = forecasts;
        this.context = context;
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.weather_card, parent, false);

        return new CardAdapter.CardViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        Forecast forecast = forecasts.get(position);
        holder.tvDescription.setText(forecast.getDescription().toUpperCase());
        holder.tvAverageDay.setText(String.format(AVERAGE_DAY_LAYOUT, forecast.getAverageDay()));
        holder.tvAverageNight.setText(String.format(AVERAGE_NIGHT_LAYOUT, forecast.getAverageNight()));
        holder.tvWind.setText(String.format(WIND_LAYOUT, forecast.getWind()));
        holder.tvPressure.setText(String.format(PRESSURE_LAYOUT, forecast.getPressure()));
        holder.tvHumidity.setText(String.format(HUMIDITY_LAYOUT, forecast.getHumidity()));
        holder.tvDate.setText(sdf.format(forecast.getDate()));
        String iconUrl = ICON_PATH + forecast.getIconName() + ICON_EXTENSION;
        Picasso.with(context).load(iconUrl).into(holder.ivWeatherIcon);
    }

    @Override
    public int getItemCount() {
        return forecasts.size();
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {

        final TextView tvDescription;
        final TextView tvAverageDay;
        final TextView tvAverageNight;
        final TextView tvWind;
        final TextView tvPressure;
        final TextView tvHumidity;
        final TextView tvDate;
        final ImageView ivWeatherIcon;

        public CardViewHolder(View itemView) {
            super(itemView);
            tvDescription = (TextView) itemView.findViewById(R.id.description_tv);
            tvAverageDay = (TextView) itemView.findViewById(R.id.average_day_tv);
            tvAverageNight = (TextView) itemView.findViewById(R.id.average_night_tv);
            tvWind = (TextView) itemView.findViewById(R.id.wind_tv);
            tvPressure = (TextView) itemView.findViewById(R.id.pressure_tv);
            tvHumidity = (TextView) itemView.findViewById(R.id.humidity_tv);
            tvDate = (TextView) itemView.findViewById(R.id.date_tv);
            ivWeatherIcon = (ImageView) itemView.findViewById(R.id.weather_icon);
        }
    }
}

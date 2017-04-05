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
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        Forecast forecast = forecasts.get(position);
        holder.tvDescription.setText(forecast.getDescription().toUpperCase());
        holder.tvAverageDay.setText(String.format("Average Day: %.2f ℃", forecast.getAverageDay()));
        holder.tvAverageNight.setText(String.format("Average Night: %.2f ℃", forecast.getAverageNight()));
        holder.tvWind.setText(String.format("Wind: %.2f km/h", forecast.getWind()));
        holder.tvPressure.setText(String.format("Pressure: %.2f hPa", forecast.getPressure()));
        holder.tvHumidity.setText(String.format("Humidity: %.2f", forecast.getHumidity()) + "%");
        holder.tvDate.setText(sdf.format(forecast.getDate()));
        String iconUrl = "http://openweathermap.org/img/w/" + forecast.getIconName() + ".png";
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

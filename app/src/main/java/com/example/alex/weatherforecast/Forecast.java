package com.example.alex.weatherforecast;

import java.util.Date;

public class Forecast {

    private String description;
    private Double averageDay;
    private Double averageNight;
    private Double wind;
    private Double pressure;
    private Double humidity;
    private Date date;
    private String iconName;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getAverageDay() {
        return averageDay;
    }

    public void setAverageDay(Double averageDay) {
        this.averageDay = averageDay;
    }

    public Double getAverageNight() {
        return averageNight;
    }

    public void setAverageNight(Double averageNight) {
        this.averageNight = averageNight;
    }

    public Double getWind() {
        return wind;
    }

    public void setWind(Double wind) {
        this.wind = wind;
    }

    public Double getPressure() {
        return pressure;
    }

    public void setPressure(Double pressure) {
        this.pressure = pressure;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }
}

package com.andro.routine.weather;

/**
 * Created by andro on 02/09/17.
 */

public class Weather {

    private String time;
    private String city;
    private String summary;
    private String temperature;
    private String humidity;
    private String windSpeed;
    private String visibility;
    private String pressure;
    private String precipProbability;
    private String summaryDaily;
    private String temperatureMin;
    private String temperatureMax;

    public void setSummaryDaily(String summaryDaily) {
        this.summaryDaily = summaryDaily;
    }

    public void setTemperatureMax(String temperatureMax) {
        this.temperatureMax = temperatureMax;
    }

    public void setTemperatureMin(String temperatureMin) {
        this.temperatureMin = temperatureMin;
    }

    public void setPrecipProbability(String precipProbability) {
        this.precipProbability = precipProbability;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public void setWindSpeed(String windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getPressure() {
        return pressure;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getVisibility() {
        return visibility;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public String getCity() {
        return city;
    }

    public String getSummary() {
        return summary;
    }

    public String getTime() {
        return time;
    }

    public String getPrecipProbability() {
        return precipProbability;
    }

    public String getSummaryDaily() {
        return summaryDaily;
    }

    public String getTemperatureMax() {
        return temperatureMax;
    }

    public String getTemperatureMin() {
        return temperatureMin;
    }
}

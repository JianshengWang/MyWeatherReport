package com.myweatherreport.db;

import org.litepal.crud.DataSupport;

/**
 * Created by hp-pc on 2018/11/24.
 */

public class WeatherList extends DataSupport {
    private int id;
    private String cityName;
    private String weatherInfo;
    private String temperature;

    public WeatherList(int id, String cityName, String weatherInfo, String temperature) {
        this.id = id;
        this.cityName = cityName;
        this.weatherInfo = weatherInfo;
        this.temperature = temperature;
    }

    public int getId() { return id; }
    public void setId(int id) {this.id = id; }

    public String getCityName() {return cityName; }
    public void setCityName(String cityName) {this.cityName = cityName; }

    public String getWeatherInfo() { return weatherInfo; }
    public void setWeatherInfo(String weatherInfo) { this.weatherInfo = weatherInfo; }

    public String getTemperature() {return temperature; }
    public void setTemperature(String temperature) {this.temperature = temperature;}
}

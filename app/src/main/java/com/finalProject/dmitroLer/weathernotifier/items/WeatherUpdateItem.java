package com.finalProject.dmitroLer.weathernotifier.items;

import com.johnhiott.darkskyandroidlib.models.WeatherResponse;

/**
 * Created by EtayP on 17-Aug-17.
 */

public class WeatherUpdateItem {

    public String id;
    public WeatherResponse weatherResponse;
    public String location;

    public WeatherUpdateItem(String id, WeatherResponse weatherResponse, String location) {
        this.id = id;
        this.weatherResponse = weatherResponse;
        this.location = location;
    }
}

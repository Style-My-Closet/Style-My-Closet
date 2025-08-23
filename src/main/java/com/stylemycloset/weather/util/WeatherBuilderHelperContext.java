package com.stylemycloset.weather.util;

import com.stylemycloset.location.Location;
import com.stylemycloset.weather.entity.Humidity;
import com.stylemycloset.weather.entity.Precipitation;
import com.stylemycloset.weather.entity.Temperature;
import com.stylemycloset.weather.entity.Weather;
import com.stylemycloset.weather.entity.Weather.AlertType;
import com.stylemycloset.weather.entity.WindSpeed;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


public class WeatherBuilderHelperContext {
    public LocalDateTime forecastedAt;
    public LocalDateTime forecastAt;
    public Location location;


    public Weather.SkyStatus skyStatus = Weather.SkyStatus.CLEAR;
    public Precipitation precipitation = new Precipitation(AlertType.NONE, 0.0, 0.0);
    public Temperature temperature = new Temperature(0.0, 0.0, 0.0, 0.0);
    public Humidity humidity = new Humidity(0.0, 0.0);
    public WindSpeed windSpeed = new WindSpeed(0.0, 0.0);
    public boolean isAlertTriggered = false;
    public Weather.AlertType alertType = Weather.AlertType.NONE;
}


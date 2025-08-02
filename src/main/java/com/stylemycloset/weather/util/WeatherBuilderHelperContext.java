package com.stylemycloset.weather.util;

import com.stylemycloset.location.Location;
import com.stylemycloset.weather.entity.Humidity;
import com.stylemycloset.weather.entity.Precipitation;
import com.stylemycloset.weather.entity.Temperature;
import com.stylemycloset.weather.entity.Weather;
import com.stylemycloset.weather.entity.WindSpeed;
import java.time.LocalDateTime;



public class WeatherBuilderHelperContext {
    public LocalDateTime forecastedAt;
    public LocalDateTime forecastAt;
    public Location location;

    public Weather.SkyStatus skyStatus = Weather.SkyStatus.CLEAR;
    public Precipitation precipitation = new Precipitation(null, 0.0, 0.0);
    public Temperature temperature = new Temperature(null, null, null, null);
    public Humidity humidity = new Humidity(null, null);
    public WindSpeed windSpeed = new WindSpeed(null, null);
    public boolean isAlertTriggered = false;
    public Weather.AlertType alertType = Weather.AlertType.NONE;
}


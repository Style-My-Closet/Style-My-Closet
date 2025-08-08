package com.stylemycloset.weather.service;

import com.stylemycloset.weather.dto.WeatherAPILocation;
import com.stylemycloset.weather.dto.WeatherDto;
import com.stylemycloset.weather.entity.Weather;
import java.util.List;

public interface WeatherService {
    public List<WeatherDto> getWeatherByCoordinates(double latitude, double longitude);
    public WeatherAPILocation getLocation(Double latitude, Double longitude);
    public void checkWeather(Double latitude, Double longitude);
}
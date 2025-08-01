package com.stylemycloset.weather.service;

import com.stylemycloset.weather.dto.WeatherDto;
import java.util.List;

public interface WeatherService {
    public List<WeatherDto> getWeatherByCoordinates(double latitude, double longitude);
}
package com.stylemycloset.weather.service;

import com.stylemycloset.weather.dto.WeatherDto;
import com.stylemycloset.weather.entity.Weather;
import com.stylemycloset.weather.mapper.WeatherMapper;
import com.stylemycloset.weather.repository.WeatherRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final WeatherRepository weatherRepository;
    private final WeatherMapper weatherMapper;

    public List<WeatherDto> getWeatherByCoordinates(double latitude, double longitude) {
        List<Weather> weathers = weatherRepository.findByLocation(latitude, longitude);
        return weathers.stream()
            .map(weatherMapper::toDto)
            .toList();
    }


}


package com.stylemycloset.weather.service;

import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import com.stylemycloset.location.Location;
import com.stylemycloset.location.LocationRepository;
import com.stylemycloset.weather.dto.WeatherAPILocation;
import com.stylemycloset.weather.dto.WeatherDto;
import com.stylemycloset.weather.entity.Weather;
import com.stylemycloset.weather.mapper.LocationMapper;
import com.stylemycloset.weather.mapper.WeatherMapper;
import com.stylemycloset.weather.repository.WeatherRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final WeatherRepository weatherRepository;
    private final WeatherMapper weatherMapper;
    private final LocationMapper locationMapper;
    private final LocationRepository locationRepository;

    public List<WeatherDto> getWeatherByCoordinates(double latitude, double longitude) {
        List<Weather> weathers = weatherRepository.findByLocation(latitude, longitude);
        return weathers.stream()
            .map(weatherMapper::toDto)
            .toList();
    }

    public WeatherAPILocation getLocation(Double latitude, Double longitude) {
        Location location = locationRepository.findByLatitudeAndLongitude(latitude,longitude).orElseThrow(
            ()->  new StyleMyClosetException(ErrorCode.ERROR_CODE, Map.of("weather", "weather")  ));
        return locationMapper.toDto(location);
    }
}


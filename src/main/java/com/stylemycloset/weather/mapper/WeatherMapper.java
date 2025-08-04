package com.stylemycloset.weather.mapper;

import com.stylemycloset.weather.dto.WeatherDto;
import com.stylemycloset.weather.entity.Weather;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
public class WeatherMapper {

    public WeatherDto toDto(Weather weather) {


        return new WeatherDto(
            weather.getId(),
            weather.getForecastedAt(),
            weather.getForecastAt(),
            weather.getLocation(),
            weather.getSkyStatus(),
            weather.getPrecipitation(),
            weather.getHumidity(),
            weather.getTemperature(),
            weather.getWindSpeed()
        );
    }

    public List<WeatherDto> toDtoList(List<Weather> weathers) {
        if (weathers == null) {
            return Collections.emptyList();
        }

        return weathers.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
}

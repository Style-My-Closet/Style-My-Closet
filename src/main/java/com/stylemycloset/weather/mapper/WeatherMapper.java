package com.stylemycloset.weather.mapper;

import com.stylemycloset.weather.dto.WeatherDto;
import com.stylemycloset.weather.dto.WindSpeedDto;
import com.stylemycloset.weather.entity.Weather;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
public class WeatherMapper {

    public WeatherDto toDto(Weather weather) {


        return new WeatherDto(
            weather.getId(),
            weather.getForecastedAt().atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            weather.getForecastAt().atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            weather.getLocation(),
            weather.getSkyStatus(),
            WeatherInfosMapper.toDto(weather.getPrecipitation()) ,
            WeatherInfosMapper.toDto(weather.getHumidity())  ,
            WeatherInfosMapper.toDto(weather.getTemperature()) ,
            WeatherInfosMapper.toDto(weather.getWindSpeed())
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

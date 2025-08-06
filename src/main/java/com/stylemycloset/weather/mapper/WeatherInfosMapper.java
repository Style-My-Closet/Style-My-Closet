package com.stylemycloset.weather.mapper;

import com.stylemycloset.weather.dto.HumidityDto;
import com.stylemycloset.weather.dto.PrecipitationDto;
import com.stylemycloset.weather.dto.TemperatureDto;
import com.stylemycloset.weather.dto.WindSpeedDto;
import com.stylemycloset.weather.entity.Humidity;
import com.stylemycloset.weather.entity.Precipitation;
import com.stylemycloset.weather.entity.Temperature;
import com.stylemycloset.weather.entity.WindSpeed;
import org.springframework.stereotype.Component;


public class WeatherInfosMapper {

    public static PrecipitationDto toDto(Precipitation entity) {

        return new PrecipitationDto(
            entity.getType(),
            entity.getAmount(),
            entity.getProbability()
        );
    }


    public static TemperatureDto toDto(Temperature entity) {
        if (entity == null) return null;
        return new TemperatureDto(
            entity.getCurrent(),
            entity.getComparedToDayBefore(),
            entity.getMin(),
            entity.getMax()
        );
    }

    public static WindSpeedDto toDto(WindSpeed entity) {

        return new WindSpeedDto(
            entity.getCurrent(),
            entity.mapWindStrength(entity.getCurrent())
        );
    }


    public static HumidityDto toDto(Humidity entity) {

        return new HumidityDto(
            entity.getCurrent(),
            entity.getComparedToDayBefore()
        );
    }

}


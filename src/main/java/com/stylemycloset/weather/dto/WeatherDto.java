package com.stylemycloset.weather.dto;

import com.stylemycloset.location.Location;
import com.stylemycloset.weather.entity.Humidity;
import com.stylemycloset.weather.entity.Precipitation;
import com.stylemycloset.weather.entity.Temperature;
import com.stylemycloset.weather.entity.Weather.SkyStatus;
import com.stylemycloset.weather.entity.WindSpeed;
import java.time.LocalDateTime;
import java.util.UUID;

public record WeatherDto(
    Long id,
    LocalDateTime forecastedAt,
    LocalDateTime forecastAt,
    Location location,
    SkyStatus skyStatus,
    PrecipitationDto precipitation,
    HumidityDto humidity,
    TemperatureDto temperature,
    WindSpeedDto windSpeed
) {}


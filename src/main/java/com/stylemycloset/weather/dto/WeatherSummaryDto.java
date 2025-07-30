package com.stylemycloset.weather.dto;

import com.stylemycloset.weather.entity.Precipitation;
import com.stylemycloset.weather.entity.Temperature;
import com.stylemycloset.weather.entity.Weather.SkyStatus;
import java.util.UUID;

public record WeatherSummaryDto(
    UUID weatherId,
    SkyStatus skyStatus,
    Precipitation precipitation,
    Temperature temperature
) {}

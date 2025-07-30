package com.stylemycloset.weather.dto;

import com.stylemycloset.weather.entity.Precipitation;
import com.stylemycloset.weather.entity.Temperature;
import java.util.UUID;

public record WeatherSummaryDto(
    UUID weatherId,
    String skyStatus,
    Precipitation precipitation,
    Temperature temperature
) {}

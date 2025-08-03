package com.stylemycloset.weather.dto;

import com.stylemycloset.weather.entity.Weather.AlertType;

public record PrecipitationDto(
    AlertType type,
    Double amount,
    Double probability
) {}
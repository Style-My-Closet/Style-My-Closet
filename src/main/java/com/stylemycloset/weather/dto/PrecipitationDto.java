package com.stylemycloset.weather.dto;

public record PrecipitationDto(
    String type,
    Double amount,
    Double probability
) {}
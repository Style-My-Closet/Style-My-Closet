package com.stylemycloset.weather.dto;

public record HumidityDto(
    Double current,
    Double comparedToDayBefore
) {}
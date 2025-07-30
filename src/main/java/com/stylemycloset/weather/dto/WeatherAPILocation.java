package com.stylemycloset.weather.dto;

import java.util.List;

public record WeatherAPILocation(
    Double latitude,
    Double longitude,
    Integer x,
    Integer y,
    List<String> locationNames
) {}
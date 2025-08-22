package com.stylemycloset.weather.dto;

import java.util.List;

public record WeatherAPILocation(
    double latitude,
    double longitude,
    int x,
    int y,
    List<String> locationNames
) {}
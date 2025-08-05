package com.stylemycloset.weather.dto;

import java.util.List;

public record LocationInfo(double x, double y, List<String> locationNames) {}


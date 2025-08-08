package com.stylemycloset.weather.dto;

public record WeatherAlertEvent(Long receiverId,
                                Long weatherId,
                                String message) {}

package com.stylemycloset.weather.service;

import com.stylemycloset.sse.service.SseService;
import com.stylemycloset.weather.dto.WeatherAlertEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeatherAlertEventListener {
    private final SseService sseService;


    @EventListener
    public void onWeatherAlert(WeatherAlertEvent event) {
        sseService.sendWeatherAlert(event.weatherId(), event.message());
    }
}

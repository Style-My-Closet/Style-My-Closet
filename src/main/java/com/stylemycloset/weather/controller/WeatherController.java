package com.stylemycloset.weather.controller;

import com.stylemycloset.weather.dto.WeatherDto;
import com.stylemycloset.weather.service.WeatherServiceImpl;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weathers")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherServiceImpl weatherService;

    @GetMapping
    public ResponseEntity<List<WeatherDto>> getWeathers(
        @RequestParam double latitude,
        @RequestParam double longitude
    ) {
        List<WeatherDto> weathers = weatherService.getWeatherByCoordinates(latitude, longitude);
        return ResponseEntity.ok(weathers);
    }
}
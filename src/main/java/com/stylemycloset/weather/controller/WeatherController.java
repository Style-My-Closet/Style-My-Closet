package com.stylemycloset.weather.controller;

import com.stylemycloset.security.ClosetUserDetails;
import com.stylemycloset.weather.dto.WeatherAPILocation;
import com.stylemycloset.weather.dto.WeatherDto;
import com.stylemycloset.weather.service.WeatherService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weathers")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping
    public ResponseEntity<List<WeatherDto>> getWeathers(
        @RequestParam double latitude,
        @RequestParam double longitude,
        @AuthenticationPrincipal ClosetUserDetails principal
    ) {
        weatherService.checkWeather(latitude, longitude, principal.getUserId());
        List<WeatherDto> weathers = weatherService.getWeatherByCoordinates(latitude, longitude);
        return ResponseEntity.ok(weathers);
    }

    @GetMapping("/location")
    public ResponseEntity<WeatherAPILocation> getWeatherLocation(
        @RequestParam double latitude,
        @RequestParam double longitude
    ){
        WeatherAPILocation location = weatherService.getLocation(latitude,longitude);
        return ResponseEntity.ok(location);
    }
}
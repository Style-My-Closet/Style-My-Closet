package com.stylemycloset.weather.controller;

import com.stylemycloset.weather.dto.WeatherAPILocation;
import com.stylemycloset.weather.dto.WeatherDto;
import com.stylemycloset.weather.service.WeatherService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
  private final JobLauncher jobLauncher;
  private final Job weatherJob;

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<WeatherDto>> getWeathers(
      @RequestParam(name = "longitude") double longitude,
      @RequestParam(name = "latitude") double latitude,
      @AuthenticationPrincipal(expression = "userId") Long userId
  ) {
    List<WeatherDto> weathers = weatherService.getWeatherByCoordinates(latitude, longitude);
    weatherService.checkWeather(latitude, longitude, userId);

    return ResponseEntity.ok(weathers);
  }

  @GetMapping("/location")
  public ResponseEntity<WeatherAPILocation> getWeatherLocation(
      @RequestParam(name = "longitude") double longitude,
      @RequestParam(name = "latitude") double latitude
  ) {
    WeatherAPILocation location = weatherService.getLocation(latitude, longitude);

    return ResponseEntity.ok(location);
  }
}
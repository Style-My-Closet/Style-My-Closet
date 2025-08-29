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

@Slf4j
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
    log.info("[WeatherAPI] 요청 - userId: {}, 위도: {}, 경도: {}",
        userId, latitude, longitude);
    List<WeatherDto> weathers = weatherService.getWeatherByCoordinates(latitude, longitude);
    log.info("[WeatherAPI] 조회된 날씨 데이터: {}", weathers);
    weatherService.checkWeather(latitude, longitude, userId);

    log.info("[WeatherAPI] 응답 - userId: {}, 데이터 개수: {}", userId, weathers.size());
    return ResponseEntity.ok(weathers);
  }

  @GetMapping("/location")
  public ResponseEntity<WeatherAPILocation> getWeatherLocation(
      @RequestParam(name = "longitude") double longitude,
      @RequestParam(name = "latitude") double latitude
  ) {

    log.info("[WeatherLocation] 요청 - 위도: {}, 경도: {}", latitude, longitude);
    WeatherAPILocation location = weatherService.getLocation(latitude, longitude);
    log.info("[WeatherLocation] 응답 - 요청 위도: {}, 경도: {}, 반환 위치명: {}, 행정구역코드: {}",
        latitude, longitude,
        location.x(),
        location.y());

    return ResponseEntity.ok(location);
  }
}
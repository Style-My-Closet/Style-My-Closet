package com.stylemycloset.weather.controller;

import com.stylemycloset.weather.dto.WeatherAPILocation;
import com.stylemycloset.weather.dto.WeatherDto;
import com.stylemycloset.weather.service.KakaoApiService;
import com.stylemycloset.weather.service.WeatherService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
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
        @RequestParam double latitude,
        @RequestParam double longitude,
        @AuthenticationPrincipal(expression = "userId") Long userId
    ) {
        weatherService.checkWeather(latitude, longitude, userId);
        List<WeatherDto> weathers = weatherService.getWeatherByCoordinates(latitude, longitude);
        return ResponseEntity.ok(weathers);
    }

    @GetMapping("/location")
    public ResponseEntity<WeatherAPILocation> getWeatherLocation(
        @RequestParam double longitude,
        @RequestParam double latitude
    )
        throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        JobParameters params = new JobParametersBuilder()
            .addString("lat", String.valueOf(latitude))
            .addString("lon", String.valueOf(longitude))
            .addLong("time", System.currentTimeMillis()) // Job 중복 실행 방지
            .toJobParameters();

        jobLauncher.run(weatherJob, params);

        WeatherAPILocation location = weatherService.getLocation(latitude,longitude);
        return ResponseEntity.ok(location);
    }
}
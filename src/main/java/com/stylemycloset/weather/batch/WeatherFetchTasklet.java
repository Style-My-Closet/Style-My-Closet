package com.stylemycloset.weather.batch;

import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import com.stylemycloset.weather.entity.Weather;
import com.stylemycloset.weather.repository.WeatherRepository;
import com.stylemycloset.weather.service.ForecastApiService;
import com.stylemycloset.weather.service.KakaoApiService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeatherFetchTasklet implements Tasklet {

    private final ForecastApiService forecastApiService;
    private final WeatherRepository weatherRepository;
    private final KakaoApiService kakaoApiService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        double lat = 37.5665;    // 예시 좌표 (서울)
        double lon = 126.9780;

        Weather weather = forecastApiService.fetchData(kakaoApiService.createLocation(lon,lat));

        weatherRepository.save(Optional.ofNullable(weather).orElseThrow(() ->
            new StyleMyClosetException(ErrorCode.ERROR_CODE, Map.of("weather", "latestWeather"))));

        return RepeatStatus.FINISHED;
    }
}


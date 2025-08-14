package com.stylemycloset.weather.batch;

import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import com.stylemycloset.location.Location;
import com.stylemycloset.location.LocationRepository;
import com.stylemycloset.weather.entity.Weather;
import com.stylemycloset.weather.repository.WeatherRepository;
import com.stylemycloset.weather.service.ForecastApiService;
import com.stylemycloset.weather.service.KakaoApiService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobParameters;
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
    private final LocationRepository locationRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        JobParameters params = chunkContext.getStepContext()
            .getStepExecution()
            .getJobParameters();

        double lat = Double.parseDouble(Objects.requireNonNull(params.getString("lat")));
        double lon = Double.parseDouble(Objects.requireNonNull(params.getString("lon")));

        Location location = locationRepository.findByLatitudeAndLongitude(lat,lon).orElseGet(
            ()->kakaoApiService.createLocation(lon,lat)  );

        Optional.ofNullable(location)
            .orElseThrow(() -> new StyleMyClosetException(ErrorCode.ERROR_CODE,Map.of("location 없음","null")));
        Weather weather = forecastApiService.fetchData
            (location);

        weatherRepository.save(Optional.ofNullable(weather).orElseThrow(() ->
            new StyleMyClosetException(ErrorCode.ERROR_CODE, Map.of("weather", "latestWeather"))));

        return RepeatStatus.FINISHED;
    }
}


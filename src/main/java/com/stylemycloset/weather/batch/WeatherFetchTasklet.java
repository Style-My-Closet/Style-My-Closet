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

    // 기본 서울 좌표
    private static final double DEFAULT_LATITUDE = 37.5665;
    private static final double DEFAULT_LONGITUDE = 126.9780;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        // 최신 Location 가져오기
        Optional<Location> latestLocationOpt = locationRepository.findTopByOrderByCreatedAtDesc();

        // 위도/경도 변수에 값 할당
        double lat = latestLocationOpt.map(Location::getLatitude).orElse(DEFAULT_LATITUDE);
        double lon = latestLocationOpt.map(Location::getLongitude).orElse(DEFAULT_LONGITUDE);

        Location location = locationRepository.findByLatitudeAndLongitude(lat,lon).orElseGet(
            ()->kakaoApiService.createLocation(lon,lat)  );

        Optional.ofNullable(location)
            .orElseThrow(() -> new StyleMyClosetException(ErrorCode.ERROR_CODE,Map.of("location 없음","null")));

        List<Weather> weathers = forecastApiService.fetchData
            (location);

        weatherRepository.saveAll(Optional.of(weathers).orElseThrow(
            ()-> new StyleMyClosetException(ErrorCode.ERROR_CODE,Map.of("weather","list"))
        ) );

        return RepeatStatus.FINISHED;
    }
}


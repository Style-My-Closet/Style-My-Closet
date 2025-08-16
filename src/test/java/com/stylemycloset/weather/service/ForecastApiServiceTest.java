package com.stylemycloset.weather.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stylemycloset.location.Location;
import com.stylemycloset.location.LocationRepository;
import com.stylemycloset.weather.entity.Humidity;
import com.stylemycloset.weather.entity.Temperature;
import com.stylemycloset.weather.entity.Weather;
import com.stylemycloset.weather.processor.HumidityProcessor;
import com.stylemycloset.weather.processor.TmpProcessor;
import com.stylemycloset.weather.processor.WeatherCategoryProcessor;
import com.stylemycloset.weather.repository.WeatherRepository;
import com.stylemycloset.weather.util.WeatherBuilderHelperContext;
import com.stylemycloset.weather.util.WeatherItemsFilterer;
import com.stylemycloset.weather.utils.FakeWeatherApiFetcher;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("forecast")
class ForecastApiServiceTest {

    @Mock
    private WeatherRepository weatherRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private WeatherItemsFilterer deduplicator;

    private TmpProcessor tmpProcessor;
    private HumidityProcessor humidProcessor;

    @Mock
    private WeatherBuilderHelperContext ctx;

    private FakeWeatherApiFetcher apiFetcher;


    private ForecastApiService forecastApiService;

    private final Location location = Location.builder()
        .x(55).y(127)
        .latitude(37.5665)
        .longitude(126.9780)
        .locationNames(List.of("서울", "중구"))
        .build();

    @BeforeEach
    void setUp() {
        // weatherRepository는 Mockito mock 상태
        // processor는 실제 객체 생성, weatherRepository 주입
        tmpProcessor = Mockito.spy(new TmpProcessor(weatherRepository));
        humidProcessor = Mockito.spy(new HumidityProcessor(weatherRepository));

        apiFetcher = new FakeWeatherApiFetcher(new ObjectMapper());
        List<WeatherCategoryProcessor> processors = List.of(tmpProcessor, humidProcessor);

        forecastApiService = new ForecastApiService(
            apiFetcher,
            deduplicator,
            locationRepository,
            processors
        );

        // deduplicator 설정 (mock)
        when(deduplicator.filtering(anyList())).thenAnswer(inv -> inv.getArgument(0));

        // 필요하면 processor 내 특정 메서드만 mock 처리 가능
        doReturn(true).when(tmpProcessor).supports("TMP");
        doAnswer(invocation -> {
            WeatherBuilderHelperContext ctx = invocation.getArgument(0);
            String value = invocation.getArgument(2);
            ctx.temperature = new Temperature(Double.parseDouble(value), null, null, null);
            return null;
        }).when(tmpProcessor).process(any(), eq("TMP"), anyString());

        doReturn(true).when(humidProcessor).supports("REH");
        doAnswer(invocation -> {
            WeatherBuilderHelperContext ctx = invocation.getArgument(0);
            String value = invocation.getArgument(2);
            ctx.humidity = new Humidity(Double.parseDouble(value), null);
            return null;
        }).when(humidProcessor).process(any(), eq("REH"), anyString());
    }


    @Test
    void fetchData_shouldSaveWeatherAndLocation() {
        // when
        List<Weather> weathers = forecastApiService.fetchData(location);

        weatherRepository.saveAll(weathers);
        // then
        verify(weatherRepository).saveAll(anyList());
        verify(locationRepository).save(any(Location.class));
    }
}


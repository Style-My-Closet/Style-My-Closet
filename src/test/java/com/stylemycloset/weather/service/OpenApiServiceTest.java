package com.stylemycloset.weather.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
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
import com.stylemycloset.weather.util.WeatherApiFetcher;
import com.stylemycloset.weather.util.WeatherBuilderHelperContext;
import com.stylemycloset.weather.util.WeatherItemDeduplicator;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OpenApiServiceTest {

    @Mock
    private WeatherRepository weatherRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private WeatherApiFetcher apiFetcher;

    @Mock
    private WeatherItemDeduplicator deduplicator;

    @Mock private TmpProcessor tmpProcessor;
    @Mock private HumidityProcessor humidProcessor;

    @InjectMocks
    private OpenApiService openApiService;

    private final String baseDate = "20250801";
    private final String baseTime = "0200";

    private final Location location = Location.builder()
        .x(55).y(127)
        .latitude(37.5665)
        .longitude(126.9780)
        .locationNames(List.of("서울", "중구"))
        .build();

    private final List<JsonNode> rawItems = new ArrayList<>();
    private final List<JsonNode> dedupedItems = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode node1 = objectMapper.readTree("""
            {
                "baseDate": "20250801",
                "baseTime": "0200",
                "category": "TMP",
                "fcstDate": "20250801",
                "fcstTime": "0300",
                "fcstValue": "25.4",
                "nx": 55,
                "ny": 127
            }
        """);

        JsonNode node2 = objectMapper.readTree("""
            {
                "baseDate": "20250801",
                "baseTime": "0200",
                "category": "REH",
                "fcstDate": "20250801",
                "fcstTime": "0300",
                "fcstValue": "80",
                "nx": 55,
                "ny": 127
            }
        """);

        rawItems.add(node1);
        rawItems.add(node2);

        // 중복 제거 후에도 동일한 데이터라고 가정
        dedupedItems.addAll(rawItems);

        when(apiFetcher.fetchAllPages(baseDate, baseTime, location)).thenReturn(rawItems);
        when(deduplicator.deduplicate(rawItems)).thenReturn(dedupedItems);

        // TMP processor 설정
        when(tmpProcessor.supports("TMP")).thenReturn(true);
        doAnswer(invocation -> {
            WeatherBuilderHelperContext ctx = invocation.getArgument(0);
            String value = invocation.getArgument(1);
            ctx.temperature = new Temperature(Double.parseDouble(value), null, null, null);
            return null;
        }).when(tmpProcessor).process(any(), anyString());

        // REH processor 설정
        when(humidProcessor.supports("REH")).thenReturn(true);
        doAnswer(invocation -> {
            WeatherBuilderHelperContext ctx = invocation.getArgument(0);
            String value = invocation.getArgument(1);
            ctx.humidity = new Humidity(Double.parseDouble(value), null);
            return null;
        }).when(humidProcessor).process(any(), anyString());

        // processors 리스트를 OpenApiService에 반영
        List<WeatherCategoryProcessor> processors = List.of(tmpProcessor, humidProcessor);
        ReflectionTestUtils.setField(openApiService, "processors", processors);

    }

    @Test
    void fetchData_shouldSaveWeatherAndLocation() {
        // when
        openApiService.fetchData(baseDate, baseTime, location);

        // then
        verify(weatherRepository).save(any(Weather.class));
        verify(locationRepository).save(any(Location.class));
    }
}


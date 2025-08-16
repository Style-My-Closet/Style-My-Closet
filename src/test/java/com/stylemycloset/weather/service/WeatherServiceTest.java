package com.stylemycloset.weather.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stylemycloset.common.exception.StyleMyClosetException;
import com.stylemycloset.location.Location;
import com.stylemycloset.location.LocationRepository;
import com.stylemycloset.notification.event.domain.WeatherAlertEvent;
import com.stylemycloset.weather.dto.WeatherDto;
import com.stylemycloset.weather.entity.Humidity;
import com.stylemycloset.weather.entity.Precipitation;
import com.stylemycloset.weather.entity.Temperature;
import com.stylemycloset.weather.entity.Weather;
import com.stylemycloset.weather.entity.Weather.AlertType;
import com.stylemycloset.weather.entity.WindSpeed;
import com.stylemycloset.location.mapper.LocationMapper;
import com.stylemycloset.weather.mapper.WeatherInfosMapper;
import com.stylemycloset.weather.mapper.WeatherMapper;
import com.stylemycloset.weather.repository.WeatherRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
public class WeatherServiceTest {
    @Mock
    private WeatherRepository weatherRepository;

    @Mock
    private WeatherMapper weatherMapper;

    @InjectMocks
    private WeatherServiceImpl weatherService;

    @Mock
    private LocationMapper locationMapper;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private KakaoApiService kakaoApiService;

    @Mock
    private ForecastApiService forecastApiService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("위치로 날씨 정보 얻기")
    void testGetWeatherByCoordinates() {
        // given
        Humidity humidity = new Humidity(65.0, 3.0);
        Precipitation precipitation = new Precipitation(AlertType.RAIN, 2.0, 70.0);
        Temperature temperature = new Temperature(23.0, -1.0, 20.0, 25.0);
        WindSpeed windSpeed = new WindSpeed(5.5, 1.2);

        Location location = Location.builder()
            .latitude(37.5665)
            .longitude(126.9780)
            .x(60)
            .y(127)
            .locationNames(List.of("서울특별시", "중구"))
            .build();

        Weather weather = Weather.builder()
            .id(1L)
            .forecastedAt(LocalDateTime.now().minusHours(3))
            .forecastAt(LocalDateTime.now())
            .location(location)
            .skyStatus(Weather.SkyStatus.CLEAR)
            .precipitation(precipitation)
            .temperature(temperature)
            .humidity(humidity)
            .windSpeed(windSpeed)
            .build();

        when(weatherRepository.findByLocation(37.5665, 126.9780))
            .thenReturn(List.of(weather));

        when(weatherMapper.toDto(weather)).thenReturn(new WeatherDto(
            weather.getId(),
            weather.getForecastedAt().atZone(ZoneId.of("UTC")).toInstant(),
            weather.getForecastAt().atZone(ZoneId.of("UTC")).toInstant(),
            weather.getLocation(),
            weather.getSkyStatus(),
            WeatherInfosMapper.toDto(weather.getPrecipitation()) ,
            WeatherInfosMapper.toDto(weather.getHumidity()) ,
            WeatherInfosMapper.toDto(weather.getTemperature()) ,
            WeatherInfosMapper.toDto(weather.getWindSpeed())
        ));

        // when
        List<WeatherDto> result = weatherService.getWeatherByCoordinates(37.5665, 126.9780);
        // then
        assertEquals(1, result.size());
        assertEquals(23.0, result.get(0).temperature().current());
        assertEquals(AlertType.RAIN, result.get(0).precipitation().type());
    }

    @Test
    @DisplayName(" !오늘의! 날씨 얻기")
    void getTodayWeatherByLocation_shouldReturnWeatherForToday() {
        // given
        double lat = 37.5665;
        double lon = 126.9780;

        Weather oldWeather = mock(Weather.class);
        when(oldWeather.getCreatedAt()).thenReturn(Instant.now().minus(2, ChronoUnit.DAYS));

        Weather todayWeather = mock(Weather.class);
        when(todayWeather.getCreatedAt()).thenReturn(Instant.now()); // today

        when(weatherRepository.findByLocation(lat, lon))
            .thenReturn(List.of(oldWeather, todayWeather));

        // when
        Optional<Weather> result = weatherService.getTodayWeatherByLocation(lat, lon);

        // then
        assertTrue(result.isPresent());
        assertEquals(todayWeather, result.get());
    }

    @Test
    @DisplayName("AlertIsTriggered=true일때 pushlishEvent 작동")
    void checkWeather_shouldPublishEvent_whenAlertIsTriggered() {
        // given
        double lat = 37.5665;
        double lon = 126.9780;

        Weather weather = mock(Weather.class);
        when(weather.getCreatedAt()).thenReturn(Instant.now());
        when(weather.getIsAlertTriggered()).thenReturn(true);
        when(weather.getAlertType()).thenReturn(AlertType.HEAVY_RAIN);
        when(weather.getTemperature()).thenReturn(new Temperature(31.5, 2.0, 0.0, 32.1));
        when(weather.getId()).thenReturn(123L);

        when(weatherRepository.findByLocation(lat, lon)).thenReturn(List.of(weather));

        // when
        weatherService.checkWeather(lat, lon, 1L);

        // then
        ArgumentCaptor<WeatherAlertEvent> eventCaptor = ArgumentCaptor.forClass(WeatherAlertEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        WeatherAlertEvent capturedEvent = eventCaptor.getValue();
        assertEquals(123L, capturedEvent.weatherId());
        assertTrue(capturedEvent.message().contains("🌧 매우 강한 비")); // message 확인
    }

    @Test
    @DisplayName("특정 위치에 날씨 데이터 없을 때 예외처리")
    void checkWeather_shouldThrowException_whenNoTodayWeather() {
        // given
        double lat = 37.0;
        double lon = 127.0;

        when(weatherRepository.findByLocation(lat, lon)).thenReturn(Collections.emptyList());

        // when & then
        assertThrows(StyleMyClosetException.class, () ->
            weatherService.checkWeather(lat, lon, 1L)
        );
    }


}
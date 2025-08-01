package com.stylemycloset.weather.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.stylemycloset.location.Location;
import com.stylemycloset.weather.dto.WeatherDto;
import com.stylemycloset.weather.entity.Humidity;
import com.stylemycloset.weather.entity.Precipitation;
import com.stylemycloset.weather.entity.Temperature;
import com.stylemycloset.weather.entity.Weather;
import com.stylemycloset.weather.entity.WindSpeed;
import com.stylemycloset.weather.mapper.WeatherMapper;
import com.stylemycloset.weather.repository.WeatherRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WeatherServiceTest {
    @Mock
    private WeatherRepository weatherRepository;

    @Mock
    private WeatherMapper weatherMapper;

    @InjectMocks
    private WeatherServiceImpl weatherService;


    @Test
    void testGetWeatherByCoordinates() {
        // given
        Humidity humidity = new Humidity(65.0, 3.0);
        Precipitation precipitation = new Precipitation("RAIN", 2.0, 70.0);
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
            weather.getForecastedAt(),
            weather.getForecastAt(),
            weather.getLocation(),
            weather.getSkyStatus(),
            weather.getPrecipitation(),
            weather.getHumidity(),
            weather.getTemperature(),
            weather.getWindSpeed()
        ));

        // when
        List<WeatherDto> result = weatherService.getWeatherByCoordinates(37.5665, 126.9780);
            //weatherService.getWeatherByCoordinates(37.5665, 126.9780);

        // then
        assertEquals(1, result.size());
        assertEquals(23.0, result.get(0).temperature().getCurrent());
        assertEquals("RAIN", result.get(0).precipitation().getType());
    }
}
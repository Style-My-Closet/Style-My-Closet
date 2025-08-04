package com.stylemycloset.weather.controller;


import static org.hamcrest.Matchers.hasSize;

import static org.junit.jupiter.api.AssertionsKt.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import com.stylemycloset.location.Location;
import com.stylemycloset.weather.dto.WeatherDto;
import com.stylemycloset.weather.entity.*;
import com.stylemycloset.weather.entity.Weather.AlertType;
import com.stylemycloset.weather.entity.Weather.SkyStatus;
import com.stylemycloset.weather.mapper.WeatherMapper;
import com.stylemycloset.weather.repository.WeatherRepository;
import com.stylemycloset.weather.service.WeatherService;
import com.stylemycloset.weather.service.WeatherServiceImpl;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class WeatherControllerTest {


    private MockMvc mockMvc;

    @Mock
    private WeatherService weatherService;

    private WeatherMapper weatherMapper = new WeatherMapper();


    @InjectMocks
    private WeatherController weatherController;

    @BeforeEach
    void setUp() {
        // 진짜 구현체 생성, 내부 필드는 mock

        // Controller에 직접 구현체 주입
        weatherController = new WeatherController(weatherService);

        mockMvc = MockMvcBuilders.standaloneSetup(weatherController).build();
    }



    @Test
    void getWeathers_shouldReturnWeatherList() throws Exception {
        // given
        double latitude = 37.5;
        double longitude = 127.0;

        Humidity humidity = new Humidity(65.0, 3.0);
        Precipitation precipitation = new Precipitation("RAIN", 2.0, 70.0);
        Temperature temperature = new Temperature(23.0, -1.0, 20.0, 25.0);
        WindSpeed windSpeed = new WindSpeed(5.5, 1.2);

        Location location = Location.builder()
            .latitude(37.5)
            .longitude(127.0)
            .x(60)
            .y(127)
            .locationNames(List.of("서울특별시", "중구"))
            .build();

        Weather weather = Weather.builder()
            .forecastedAt(LocalDateTime.now().minusHours(3))
            .forecastAt(LocalDateTime.now())
            .location(location)
            .skyStatus(SkyStatus.CLEAR)
            .precipitation(precipitation)
            .temperature(temperature)
            .humidity(humidity)
            .windSpeed(windSpeed)
            .isAlertTriggered(true)
            .alertType(AlertType.HEAVY_RAIN)
            .build();

        WeatherDto dto = weatherMapper.toDto(weather);
        assertNotNull(dto); // 방어적으로 확인

        WeatherDto dummyDto = new WeatherDto(
            1L,
            LocalDateTime.now().minusHours(3),
            LocalDateTime.now(),
            location,
            SkyStatus.CLOUDY,
            precipitation,
            humidity,
            temperature,
            windSpeed
        );


        List<WeatherDto> dummyList = List.of(dummyDto); // null이면 여기서 터짐!


        Mockito.when(weatherService.getWeatherByCoordinates(latitude, longitude))
            .thenReturn(dummyList);

        // when & then
        mockMvc.perform(get("/api/weathers")
                .param("latitude", String.valueOf(latitude))
                .param("longitude", String.valueOf(longitude)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].location.latitude").value(latitude))
            .andExpect(jsonPath("$[0].location.longitude").value(longitude))
            .andExpect(jsonPath("$[0].skyStatus").value("CLOUDY"))

            // Humidity
            .andExpect(jsonPath("$[0].humidity.current").value(65.0))
            .andExpect(jsonPath("$[0].humidity.comparedToDayBefore").value(3.0))

            // Precipitation
            .andExpect(jsonPath("$[0].precipitation.type").value("RAIN"))
            .andExpect(jsonPath("$[0].precipitation.amount").value(2.0))
            .andExpect(jsonPath("$[0].precipitation.probability").value(70.0))

            // Temperature
            .andExpect(jsonPath("$[0].temperature.current").value(23.0))
            .andExpect(jsonPath("$[0].temperature.comparedToDayBefore").value(-1.0))
            .andExpect(jsonPath("$[0].temperature.min").value(20.0))
            .andExpect(jsonPath("$[0].temperature.max").value(25.0))

            // WindSpeed
            .andExpect(jsonPath("$[0].windSpeed.current").value(5.5))
            .andExpect(jsonPath("$[0].windSpeed.comparedToDayBefore").value(1.2));
    }
}

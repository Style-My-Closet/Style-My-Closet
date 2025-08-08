package com.stylemycloset.weather.util;

import com.stylemycloset.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherScheduler {

    private final WeatherService weatherService;

    //Geolocation API 구현해야 이용가능!
    /*@Scheduled(fixedRate = 10 * 60 * 1000) // 10분마다 실행
    public void checkWeatherChanges() {
        try {


            weatherService.checkWeather(weatherId??); // 날씨 변화 감지 및 이벤트 발행
        } catch (Exception e) {
            log.error("날씨 변화 감지 중 에러 발생", e);
        }
    }*/
}

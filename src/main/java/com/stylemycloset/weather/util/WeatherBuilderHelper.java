package com.stylemycloset.weather.util;

import com.stylemycloset.location.Location;
import com.stylemycloset.weather.entity.Humidity;
import com.stylemycloset.weather.entity.Precipitation;
import com.stylemycloset.weather.entity.Temperature;
import com.stylemycloset.weather.entity.Weather;
import com.stylemycloset.weather.entity.WindSpeed;
import com.stylemycloset.weather.processor.WeatherCategoryProcessor;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class WeatherBuilderHelper {

    private final WeatherBuilderHelperContext context;
    private final List<WeatherCategoryProcessor> processors;

    public WeatherBuilderHelper(String baseDate, String baseTime, String fcstDate, String fcstTime,
        Location location, List<WeatherCategoryProcessor> processors) {
        this.processors = processors;
        this.context = new WeatherBuilderHelperContext();
        this.context.forecastedAt = parseDateTime(baseDate, baseTime);
        this.context.forecastAt = parseDateTime(fcstDate, fcstTime);
        this.context.location = location;
    }

    public void setCategoryValue(String category, String fcstValue) {
        for (WeatherCategoryProcessor processor : processors) {
            if (processor.supports(category)) {
                processor.process(context, fcstValue);
                return;
            }
        }
        log.warn("지원하지 않는 카테고리: {}", category);
    }

    public Weather build() {
        return Weather.builder()
            .forecastedAt(context.forecastedAt)
            .forecastAt(context.forecastAt)
            .location(context.location)
            .skyStatus(context.skyStatus)
            .precipitation(context.precipitation)
            .temperature(context.temperature)
            .humidity(context.humidity)
            .windSpeed(context.windSpeed)
            .isAlertTriggered(context.isAlertTriggered)
            .alertType(context.alertType)
            .build();
    }

    private LocalDateTime parseDateTime(String dateStr, String timeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        return LocalDateTime.parse(dateStr + timeStr, formatter);
    }
}

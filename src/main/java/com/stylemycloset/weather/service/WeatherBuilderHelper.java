package com.stylemycloset.weather.service;

import com.stylemycloset.location.Location;
import com.stylemycloset.weather.entity.Humidity;
import com.stylemycloset.weather.entity.Precipitation;
import com.stylemycloset.weather.entity.Temperature;
import com.stylemycloset.weather.entity.Weather;
import com.stylemycloset.weather.entity.WindSpeed;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class WeatherBuilderHelper {
    private final LocalDateTime forecastedAt; // 예보 발표 시점
    private final LocalDateTime forecastAt;   // 예보 적용 시점
    private final Location location;

    private Weather.SkyStatus skyStatus = Weather.SkyStatus.CLEAR;

    // 임베디드 객체 초기값을 null 대신 명확히 초기화(기본값 또는 null 허용)
    private Precipitation precipitation = new Precipitation(null, 0.0, 0.0);
    private Temperature temperature = new Temperature(null, null, null, null);
    private Humidity humidity = new Humidity(null, null);
    private WindSpeed windSpeed = new WindSpeed(null, null);

    private final Boolean isAlertTriggered = false;
    private Weather.AlertType alertType = Weather.AlertType.NONE;

    public WeatherBuilderHelper(String baseDate, String baseTime, String fcstDate, String fcstTime, Location location) {
        this.forecastedAt = parseDateTime(baseDate, baseTime);
        this.forecastAt = parseDateTime(fcstDate, fcstTime);
        this.location = location;
    }

    private LocalDateTime parseDateTime(String dateStr, String timeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        return LocalDateTime.parse(dateStr + timeStr, formatter);
    }

    public void setCategoryValue(String category, String fcstValue) {
        switch (category) {
            case "PTY": // 강수 형태 (예: 비, 눈 등) - type
                precipitation = new Precipitation(fcstValue, precipitation.getAmount(), precipitation.getProbability());
                break;
            case "PCP": // 강수량 - amount
                precipitation = new Precipitation(precipitation.getType(), parseDoubleSafe(fcstValue), precipitation.getProbability());
                break;
            case "POP": // 강수확률 - probability
                precipitation = new Precipitation(precipitation.getType(), precipitation.getAmount(), parseDoubleSafe(fcstValue));
                break;
            case "SNO": //적설량
                precipitation = new Precipitation(precipitation.getType(), parseDoubleSafe(fcstValue), precipitation.getProbability());
                break;
            case "REH": // 습도 %
                humidity = new Humidity(parseDoubleSafe(fcstValue), humidity.getComparedToDayBefore());
                break;
            case "TMP": // 기온 (현재)
                temperature = new Temperature(parseDoubleSafe(fcstValue), temperature.getComparedToDayBefore(), temperature.getMin(), temperature.getMax());
                break;
            case "TMN": // 최저기온
                temperature = new Temperature(temperature.getCurrent(), temperature.getComparedToDayBefore(), parseDoubleSafe(fcstValue), temperature.getMax());
                break;
            case "TMX": // 최고기온
                temperature = new Temperature(temperature.getCurrent(), temperature.getComparedToDayBefore(), temperature.getMin(), parseDoubleSafe(fcstValue));
                break;
            case "UUU": // 풍속
                windSpeed = new WindSpeed(parseDoubleSafe(fcstValue), windSpeed.getComparedToDayBefore());
                break;
            case "SKY": // 하늘 상태 (맑음, 흐림 등) - enum 매핑 필요
                skyStatus = parseSkyStatus(fcstValue);
                break;
            default:
                // 알 수 없는 카테고리 로깅
                log.warn("알 수 없는 카테고리: {}", category);
                break;
        }
    }

    private double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private Weather.SkyStatus parseSkyStatus(String value) {
        switch (value) {
            case "1":
                return Weather.SkyStatus.CLEAR;
            case "3":
                return Weather.SkyStatus.MOSTLY_CLOUDY;
            case "4":
                return Weather.SkyStatus.CLOUDY;
            default:
                return Weather.SkyStatus.CLEAR;
        }
    }

    public Weather build() {
        return Weather.builder()
            .forecastedAt(forecastedAt)
            .forecastAt(forecastAt)
            .location(location)
            .skyStatus(skyStatus)
            .precipitation(precipitation)
            .temperature(temperature)
            .humidity(humidity)
            .windSpeed(windSpeed)
            .isAlertTriggered(isAlertTriggered)
            .alertType(alertType)
            .build();
    }
}
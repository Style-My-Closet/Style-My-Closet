package com.stylemycloset.weather.service;

import static com.stylemycloset.location.util.LamcConverter.mapConv;

import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import com.stylemycloset.location.Location;
import com.stylemycloset.location.LocationRepository;
import com.stylemycloset.notification.event.domain.WeatherAlertEvent;
import com.stylemycloset.weather.dto.WeatherAPILocation;
import com.stylemycloset.weather.dto.WeatherDto;
import com.stylemycloset.weather.entity.Weather;
import com.stylemycloset.weather.entity.Weather.AlertType;
import com.stylemycloset.location.mapper.LocationMapper;
import com.stylemycloset.weather.mapper.WeatherMapper;
import com.stylemycloset.weather.repository.WeatherRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final WeatherRepository weatherRepository;
    private final WeatherMapper weatherMapper;
    private final LocationMapper locationMapper;
    private final LocationRepository locationRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final KakaoApiService kakaoApiService;
    private final ForecastApiService forecastApiService;

    public List<WeatherDto> getWeatherByCoordinates(double latitude, double longitude) {
        log.info("[WeatherService] getWeatherByCoordinates start lat={}, lon={}", latitude, longitude);

        List<Weather> weathers = getTheNext5DaysByLocation(latitude, longitude);
        if(weathers.isEmpty()){
            double[] xy = mapConv(longitude, latitude, 0);
            log.info("[WeatherService] 캐시 없음 -> location 조회 (x={}, y={})", xy[1], xy[0]);
            Location location = locationRepository.findByLatitudeAndLongitude((int)xy[1],(int)xy[0]).orElseGet(
                ()->kakaoApiService.createLocation(longitude,latitude)  );
            weathers = forecastApiService.fetchData(location);
            log.info("[WeatherService] forecast API 결과 size={} locationId={}", weathers.size(), location.getId());
            weatherRepository.saveAll(weathers);
            log.info("[WeatherService] forecast 데이터 저장 완료 count={}", weathers.size());
        }
        return weathers.stream()
            .map(weatherMapper::toDto)
            .toList();
    }

    public WeatherAPILocation getLocation(double latitude, double longitude) {
        log.info("[WeatherService] getLocation 호출 lat={}, lon={}", latitude, longitude);
        double[] xy = mapConv(longitude, latitude, 0);

        Location location = locationRepository.findByLatitudeAndLongitude((int)xy[1],(int)xy[0]).orElseGet(
            ()->kakaoApiService.createLocation(longitude,latitude)  );
        log.info("[WeatherService] getLocation 완료 locationId={}", location.getId());
        return locationMapper.toDto(location);
    }


    @Transactional
    public void checkWeather(double latitude, double longitude, Long userId) {
        log.info("[WeatherService] checkWeather 호출 userId={} lat={}, lon={}", userId, latitude, longitude);
        Optional<Weather> weather= getTodayWeatherByLocation(latitude, longitude);
        Weather data = weather.orElseThrow(
            ()->new StyleMyClosetException(ErrorCode.ERROR_CODE, Map.of("weather", "weather")  )
        );


        if (data.getIsAlertTriggered()) {
            log.info("[WeatherService] AlertTriggered=true type={} userId={}", data.getAlertType(), userId);
            StringBuilder messageBuilder = new StringBuilder("특수한 날씨입니다: ");

            AlertType alertType = data.getAlertType();
            double currentTemp = data.getTemperature().getCurrent();

            switch (alertType) {
                case RAIN -> messageBuilder.append("☔ 비가 오고 있음 ");
                case HEAVY_RAIN -> messageBuilder.append("🌧 매우 강한 비 ");
                case HIGH_TEMP -> messageBuilder.append("🔥 매우 더움 ");
                case LOW_TEMP -> messageBuilder.append("❄ 매우 추움 ");
                case STRONG_WIND -> messageBuilder.append("🌬 강풍 주의 ");
                default -> messageBuilder.append("알 수 없는 기상 조건 ");
            }

            // 추가 조건: 기온이 30도 초과인 경우
            if (currentTemp > 30 && alertType != AlertType.HIGH_TEMP) {
                messageBuilder.append("(현재 온도: ").append(currentTemp).append("도) ");
            }

            String message = messageBuilder.toString().trim();
            log.info("[WeatherService] WeatherAlertEvent 발행 userId={} weatherId={} message={}", userId, data.getId(), message);
            eventPublisher.publishEvent(new WeatherAlertEvent(userId, data.getId(), message));
        }
    }



    public Optional<Weather> getTodayWeatherByLocation(double latitude, double longitude) {
        log.debug("[WeatherService] getTodayWeatherByLocation 호출 lat={}, lon={}", latitude, longitude);

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul")); // 또는 시스템 기본 시간대
        Instant startOfDay = today.atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant();
        Instant endOfDay = today.plusDays(1).atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant();

        return weatherRepository.findByLocation(latitude, longitude)
            .stream()
            .filter(weather -> {
                Instant createdAt = weather.getCreatedAt();
                return !createdAt.isBefore(startOfDay) && createdAt.isBefore(endOfDay);
            })
            .findFirst(); // 또는 필요에 따라 collect(Collectors.toList())
    }

    public List<Weather> getTheNext5DaysByLocation(Double latitude, Double longitude) {
        log.debug("[WeatherService] getTheNext5DaysByLocation 호출 lat={}, lon={}", latitude, longitude);
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul")); // 한국 기준 오늘 날짜
        ZonedDateTime kstMidnight = today.atStartOfDay(ZoneId.of("Asia/Seoul")); // 한국 자정
        ZonedDateTime utcZoned = kstMidnight.withZoneSameInstant(ZoneOffset.UTC); // UTC 변환
        LocalDateTime utcTime = utcZoned.toLocalDateTime(); // DB에 넣을 값

        Instant startOfDay = today.atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant();
        Instant endOfDay = today.plusDays(1).atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant();

        List<Weather> ws1 =  weatherRepository.findTheNext5DaysByLocation(latitude, longitude, utcTime);
        log.info("[WeatherService] next5days rawCount={}", ws1.size());
        List<Weather> ws2 = ws1.stream()
            .filter(weather -> {
                Instant createdAt = weather.getCreatedAt();
                return !createdAt.isBefore(startOfDay) && createdAt.isBefore(endOfDay);
            })
            .toList();
        log.info("[WeatherService] next5days filteredCount={} (todayOnly)", ws2.size());
        return ws2;
    }
}


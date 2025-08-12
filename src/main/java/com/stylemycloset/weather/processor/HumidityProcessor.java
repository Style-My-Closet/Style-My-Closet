package com.stylemycloset.weather.processor;

import com.stylemycloset.weather.entity.Humidity;
import com.stylemycloset.weather.entity.Weather;
import com.stylemycloset.weather.repository.WeatherRepository;
import com.stylemycloset.weather.util.WeatherBuilderHelperContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class HumidityProcessor implements WeatherCategoryProcessor {

    private final WeatherRepository repository;

    @Override
    public boolean supports(String category) {
        return "REH".equals(category); // Relative Humidity
    }

    @Override
    public void process(WeatherBuilderHelperContext ctx,String category, String value) {
        double current = parseDoubleSafe(value);
        LocalDate today = LocalDate.now();
        LocalDateTime startOfYesterday = today.minusDays(1).atStartOfDay();
        LocalDateTime endOfYesterday = today.atStartOfDay().minusNanos(1);

        List<Weather> weathers = repository.findWeathersByForecastedAtYesterday(startOfYesterday, endOfYesterday);

        double yesterday;
        if (weathers.isEmpty()) {
            yesterday = 0;
        } else {
            // 예시: weathers에서 어떤 값을 계산
            // (실제 계산 로직에 맞게 수정하세요)
            yesterday = weathers.getFirst().getHumidity().getCurrent();
        }
        ctx.humidity = new Humidity(current, current-yesterday);
    }

    private double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }
}

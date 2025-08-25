package com.stylemycloset.weather.processor;

import com.stylemycloset.weather.entity.Weather;
import com.stylemycloset.weather.entity.WindSpeed;
import com.stylemycloset.weather.repository.WeatherRepository;
import com.stylemycloset.weather.util.WeatherBuilderHelperContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WindSpeedProcessor implements WeatherCategoryProcessor {

    @Override
    public boolean supports(String category) {
        return "WSD".equals(category); // Wind Speed
    }

    @Override
    public void process(WeatherBuilderHelperContext ctx,String category, String value) {

        double current = parseDoubleSafe(value);
        LocalDate today = LocalDate.now();
        LocalDateTime startOfYesterday = today.minusDays(1).atStartOfDay();
        LocalDateTime endOfYesterday = today.atStartOfDay().minusNanos(1);

        ctx.windSpeed = new WindSpeed(current, 0.0);
    }

    private double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }
}

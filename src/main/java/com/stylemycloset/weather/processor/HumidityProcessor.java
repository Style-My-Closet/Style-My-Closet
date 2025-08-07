package com.stylemycloset.weather.processor;

import com.stylemycloset.weather.entity.Humidity;
import com.stylemycloset.weather.repository.WeatherRepository;
import com.stylemycloset.weather.util.WeatherBuilderHelperContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;


@Component
public class HumidityProcessor implements WeatherCategoryProcessor {

    WeatherRepository repository;

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

        Double yesterday =  repository.findWeathersByForecastedAtYesterday
            (startOfYesterday,endOfYesterday).get(0).getHumidity().getCurrent();
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

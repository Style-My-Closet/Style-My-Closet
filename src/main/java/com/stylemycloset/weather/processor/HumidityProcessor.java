package com.stylemycloset.weather.processor;

import com.stylemycloset.weather.entity.Humidity;
import com.stylemycloset.weather.util.WeatherBuilderHelperContext;
import org.springframework.stereotype.Component;


@Component
public class HumidityProcessor implements WeatherCategoryProcessor {

    @Override
    public boolean supports(String category) {
        return "REH".equals(category); // Relative Humidity
    }

    @Override
    public void process(WeatherBuilderHelperContext ctx, String value) {
        double current = parseDoubleSafe(value);
        ctx.humidity = new Humidity(current, ctx.humidity.getComparedToDayBefore());
    }

    private double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }
}

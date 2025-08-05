package com.stylemycloset.weather.processor;

import com.stylemycloset.weather.entity.WindSpeed;
import com.stylemycloset.weather.util.WeatherBuilderHelperContext;
import org.springframework.stereotype.Component;

@Component
public class WindSpeedProcessor implements WeatherCategoryProcessor {

    @Override
    public boolean supports(String category) {
        return "WSD".equals(category); // Wind Speed
    }

    @Override
    public void process(WeatherBuilderHelperContext ctx, String value) {
        double current = parseDoubleSafe(value);
        ctx.windSpeed = new WindSpeed(current, ctx.windSpeed.getComparedToDayBefore());
    }

    private double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }
}

package com.stylemycloset.weather.processor;

import com.stylemycloset.weather.entity.Temperature;
import com.stylemycloset.weather.util.WeatherBuilderHelperContext;
import org.springframework.stereotype.Component;

@Component
public class TmpProcessor implements WeatherCategoryProcessor {

    @Override
    public boolean supports(String category) {
        return "TMP".equals(category);
    }

    @Override
    public void process(WeatherBuilderHelperContext ctx, String value) {
        double current = parseDoubleSafe(value);
        ctx.temperature = new Temperature(current, ctx.temperature.getComparedToDayBefore(), ctx.temperature.getMin(), ctx.temperature.getMax());
    }

    private double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }
}

package com.stylemycloset.weather.processor;

import com.stylemycloset.weather.entity.Precipitation;
import com.stylemycloset.weather.util.WeatherBuilderHelperContext;
import org.springframework.stereotype.Component;

@Component
public class PtyProcessor implements WeatherCategoryProcessor {

    @Override
    public boolean supports(String category) {
        return "PTY".equals(category);
    }

    @Override
    public void process(WeatherBuilderHelperContext ctx, String value) {
        ctx.precipitation = new Precipitation(value, ctx.precipitation.getAmount(), ctx.precipitation.getProbability());
    }
}

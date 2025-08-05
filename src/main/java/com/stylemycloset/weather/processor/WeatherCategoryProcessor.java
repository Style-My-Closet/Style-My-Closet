package com.stylemycloset.weather.processor;

import com.stylemycloset.weather.util.WeatherBuilderHelperContext;

public interface WeatherCategoryProcessor {
    boolean supports(String category);
    void process(WeatherBuilderHelperContext context, String value);
}

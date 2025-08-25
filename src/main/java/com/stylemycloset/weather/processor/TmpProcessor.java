package com.stylemycloset.weather.processor;

import com.stylemycloset.weather.entity.Temperature;
import com.stylemycloset.weather.entity.Weather;
import com.stylemycloset.weather.repository.WeatherRepository;
import com.stylemycloset.weather.util.WeatherBuilderHelperContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TmpProcessor implements WeatherCategoryProcessor {

    private static final Set<String> SUPPORTED = Set.of("TMP", "TMN", "TMX");

    @Override
    public boolean supports(String category) {
        return SUPPORTED.contains(category);
    }

    @Override
    public void process(WeatherBuilderHelperContext ctx, String category, String value) {


        double parsedValue = parseDoubleSafe(value);
        Temperature oldTemp = ctx.temperature;
        double current = oldTemp.getCurrent();
        double min = oldTemp.getMin();
        double max = oldTemp.getMax();



        switch (category) {
            case "TMP" -> {
                current = parsedValue;
            }
            case "TMN" -> {
                min = parsedValue;
            }
            case "TMX" -> {
                max = parsedValue;
            }
        }

        ctx.temperature = new Temperature(current,oldTemp.getComparedToDayBefore(),min,max);
    }

    private double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }
}


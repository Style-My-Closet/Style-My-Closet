package com.stylemycloset.weather.processor;

import com.stylemycloset.weather.entity.Temperature;
import com.stylemycloset.weather.repository.WeatherRepository;
import com.stylemycloset.weather.util.WeatherBuilderHelperContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class TmpProcessor implements WeatherCategoryProcessor {

    private static final Set<String> SUPPORTED = Set.of("TMP", "TMN", "TMX");
    WeatherRepository repository;

    @Override
    public boolean supports(String category) {
        return SUPPORTED.contains(category);
    }

    @Override
    public void process(WeatherBuilderHelperContext ctx, String category, String value) {
        double parsedValue = parseDoubleSafe(value);
        Temperature oldTemp = ctx.temperature;
        LocalDate today = LocalDate.now();
        LocalDateTime startOfYesterday = today.minusDays(1).atStartOfDay();
        LocalDateTime endOfYesterday = today.atStartOfDay().minusNanos(1);


        // 기존 값 유지, 해당 위치에만 값을 채운 Temperature 생성
        double current = oldTemp.getCurrent();
        Double yesterday =  repository.findWeathersByForecastedAtYesterday
            (startOfYesterday,endOfYesterday).getFirst().getTemperature().getCurrent();
        Double min = oldTemp.getMin();
        Double max = oldTemp.getMax();

        switch (category) {
            case "TMP" -> current = parsedValue;
            case "TMN" -> min = parsedValue;
            case "TMX" -> max = parsedValue;
        }

        ctx.temperature = new Temperature(current, current-yesterday, min, max);
    }

    private double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }
}


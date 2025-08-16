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
    private final WeatherRepository repository;

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
            yesterday = weathers.getFirst().getTemperature().getCurrent();
        }


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

        ctx.temperature = new Temperature(current,current-yesterday,min,max);
    }

    private double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }
}


package com.stylemycloset.weather.processor;

import com.stylemycloset.weather.entity.Precipitation;
import com.stylemycloset.weather.entity.Weather.AlertType;
import com.stylemycloset.weather.util.WeatherBuilderHelperContext;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class PrecipitationProcessor implements WeatherCategoryProcessor {

    private static final Set<String> SUPPORTED = Set.of("POP", "PTY", "PCP");

    @Override
    public boolean supports(String category) {
        return SUPPORTED.contains(category);
    }

    @Override
    public void process(WeatherBuilderHelperContext ctx, String category, String value) {
        Precipitation old = ctx.precipitation;

        Double amount = old.getAmount();
        Double probability = old.getProbability();
        AlertType alertType = ctx.alertType; // 그대로 유지

        switch (category) {
            case "PCP" -> amount = parseDoubleSafe(value);
            case "POP" -> probability = parseDoubleSafe(value);
            case "PTY" -> {
                // PTY는 강수 형태이므로 AlertType을 추론해서 바꿔줄 수도 있음
                alertType = convertToAlertType(value);
            }
        }

        ctx.precipitation = new Precipitation(alertType, amount, probability);
    }

    private double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * PTY 코드값을 AlertType으로 변환하는 로직 (예시)
     */
    private AlertType convertToAlertType(String value) {
        return switch (value) {
            case "0" -> AlertType.NONE;     // 없음
            case "1" -> AlertType.SNOW_RAIN;     // 비
            case "2" -> AlertType.SNOW; // 비/눈
            case "3" -> AlertType.SHOWER;     // 눈
            default -> AlertType.NONE;
        };
    }
}


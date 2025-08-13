package com.stylemycloset.recommendation.util;

import com.stylemycloset.weather.entity.Weather.AlertType;
import com.stylemycloset.weather.entity.Weather.SkyStatus;

public class WeatherEnumParser {

    public static AlertType parseAlertType(String str) {
        return parseEnum(AlertType.class, str);
    }

    public static SkyStatus parseSkyStatus(String str) {
        return parseEnum(SkyStatus.class, str);
    }

    private static <T extends Enum<T>> T parseEnum(Class<T> enumClass, String str) {
        if (str == null || str.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, str.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

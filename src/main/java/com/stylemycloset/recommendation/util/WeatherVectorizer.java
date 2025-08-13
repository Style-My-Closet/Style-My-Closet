package com.stylemycloset.recommendation.util;

import com.stylemycloset.weather.entity.Weather.AlertType;
import com.stylemycloset.weather.entity.Weather.SkyStatus;
import java.util.Arrays;

public class WeatherVectorizer {

    public static final int ALERT_TYPE_SIZE = AlertType.values().length;
    public static final int SKY_STATUS_SIZE = SkyStatus.values().length;

    public static float[] vectorizeAlertType(AlertType alertType) {
        float[] vector = new float[ALERT_TYPE_SIZE];
        Arrays.fill(vector, 0f);

        if (alertType != null) {
            vector[alertType.ordinal()] = 1f;
        }

        return vector;
    }

    public static float[] vectorizeSkyStatus(SkyStatus skyStatus) {
        float[] vector = new float[SKY_STATUS_SIZE];
        Arrays.fill(vector, 0f);

        if (skyStatus != null) {
            vector[skyStatus.ordinal()] = 1f;
        }

        return vector;
    }

}


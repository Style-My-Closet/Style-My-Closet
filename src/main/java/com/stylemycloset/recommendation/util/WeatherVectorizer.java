package com.stylemycloset.recommendation.util;

import com.stylemycloset.weather.entity.Weather.AlertType;
import com.stylemycloset.weather.entity.Weather.SkyStatus;
import java.util.Arrays;

public class WeatherVectorizer {

    public static final int ALERT_TYPE_SIZE = AlertType.values().length;
    public static final int SKY_STATUS_SIZE = SkyStatus.values().length;
    public static final int VECTOR_SIZE = ALERT_TYPE_SIZE + SKY_STATUS_SIZE;

    public static float[] vectorizeAlertType(AlertType alertType) {
        float[] vector = new float[VECTOR_SIZE];
        Arrays.fill(vector, 0f);

        if (alertType != null) {
            vector[alertType.ordinal()] = 1f;
        }

        return vector;
    }

    public static float[] vectorizeSkyStatus(SkyStatus skyStatus) {
        float[] vector = new float[VECTOR_SIZE];
        Arrays.fill(vector, 0f);

        if (skyStatus != null) {
            int idx = ALERT_TYPE_SIZE + skyStatus.ordinal();
            vector[idx] = 1f;
        }

        return vector;
    }

    // 두 벡터를 합치는 메서드 (null 허용)
    public static float[] vectorize(AlertType alertType, SkyStatus skyStatus) {
        float[] vector = new float[VECTOR_SIZE];
        Arrays.fill(vector, 0f);

        if (alertType != null) {
            vector[alertType.ordinal()] = 1f;
        }

        if (skyStatus != null) {
            vector[ALERT_TYPE_SIZE + skyStatus.ordinal()] = 1f;
        }

        return vector;
    }
}


package com.stylemycloset.recommendation.util;

import com.stylemycloset.recommendation.entity.ClothingCondition;
import com.stylemycloset.recommendation.repository.ClothingConditionRepository;
import com.stylemycloset.user.entity.Gender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConditionVectorizer {

    public float[] toConditionVector(ClothingCondition cc) {
        int alertVectorSize = WeatherVectorizer.ALERT_TYPE_SIZE;
        int skyVectorSize = WeatherVectorizer.SKY_STATUS_SIZE;
        int clothVectorSize = ClothingVectorizer.VECTOR_SIZE;

        int featureLength = 3  // temperature, windSpeed, humidity
            + alertVectorSize
            + skyVectorSize
            + 2  // gender, temperatureSensitivity
            + clothVectorSize;

        float[] features = new float[featureLength];
        int idx = 0;

        features[idx++] = (float) cc.getTemperature();
        features[idx++] = (float) cc.getWindSpeed();
        features[idx++] = (float) cc.getHumidity();

        float[] alertVec = WeatherVectorizer.vectorizeAlertType(cc.getWeatherType());
        for (float v : alertVec) {
            features[idx++] = v;
        }

        float[] skyVec = WeatherVectorizer.vectorizeSkyStatus(cc.getSkyStatus());
        for (float v : skyVec) {
            features[idx++] = v;
        }

        features[idx++] = cc.getGender() == Gender.MALE ? 1f : 0f;
        features[idx++] = cc.getTemperatureSensitivity() != null ? cc.getTemperatureSensitivity() : 0f;

        float[] clothVec = ClothingVectorizer.vectorize(cc.getColor(),cc.getSleeveLength(),cc.getPantsLength());
        for (float v : clothVec) {
            features[idx++] = v;
        }

        return features;
    }
}

package com.stylemycloset.recommendation.mapper;

import com.stylemycloset.cloth.entity.Cloth;
import com.stylemycloset.recommendation.dto.RecommendationDto;
import com.stylemycloset.recommendation.entity.ClothingFeature;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.weather.entity.Weather;

public class ClothingFeatureMapper {

    public static ClothingFeature fromRecommendationDto(RecommendationDto dto, Weather weather, User user) {
        return ClothingFeature.builder()
            .temperature(weather.getTemperature().getCurrent())
            .humidity(weather.getHumidity().getCurrent())
            .windSpeed(weather.getWindSpeed().getCurrent())
            .weatherType(weather.getAlertType().toString())
            .gender(user.getGender())
            .temperatureSensitivity(user.getTemperatureSensitivity())
            .label(false)
            .build();
    }

    public static ClothingFeature fromCloth(Cloth cloth, Weather weather, User user) {
        return ClothingFeature.builder()
            .temperature(weather.getTemperature().getCurrent())
            .humidity(weather.getHumidity().getCurrent())
            .windSpeed(weather.getWindSpeed().getCurrent())
            .weatherType(weather.getAlertType().toString())
            .gender(user.getGender())
            .temperatureSensitivity(user.getTemperatureSensitivity())
            .label(false)
            .build();
    }
}

package com.stylemycloset.recommendation.mapper;

import com.stylemycloset.clothes.entity.clothes.Clothes;
import com.stylemycloset.recommendation.dto.RecommendationDto;
import com.stylemycloset.recommendation.entity.ClothingCondition;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.weather.entity.Weather;

public class ClothingConditionMapper {

    public static ClothingCondition fromRecommendationDto(RecommendationDto dto, Weather weather, User user) {
        return ClothingCondition.builder()
            .temperature(weather.getTemperature().getCurrent())
            .humidity(weather.getHumidity().getCurrent())
            .windSpeed(weather.getWindSpeed().getCurrent())
            .weatherType(weather.getAlertType())
            .gender(user.getGender())
            .temperatureSensitivity(user.getTemperatureSensitivity())
            .label(false)
            .build();
    }

    public static ClothingCondition fromCloth(Clothes cloth, Weather weather, User user) {
        return ClothingCondition.builder()
            .temperature(weather.getTemperature().getCurrent())
            .humidity(weather.getHumidity().getCurrent())
            .windSpeed(weather.getWindSpeed().getCurrent())
            .weatherType(weather.getAlertType())
            .gender(user.getGender())
            .temperatureSensitivity(user.getTemperatureSensitivity())
            .label(false)
            .build();
    }
}

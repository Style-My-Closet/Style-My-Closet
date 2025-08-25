package com.stylemycloset.recommendation.mapper;

import com.stylemycloset.cloth.entity.Cloth;
import com.stylemycloset.recommendation.dto.RecommendationDto;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.weather.entity.Weather;
import java.util.List;

public class RecommendationMapper {
    public static RecommendationDto parseToRecommendationDto(List<Cloth> clothes, Weather weather ,
        User user) {
        return new RecommendationDto(
            weather.getId(),
            user.getId(),
            clothes.stream().map(ClothesMapper::toClothesDto).toList()
        );


    }
}

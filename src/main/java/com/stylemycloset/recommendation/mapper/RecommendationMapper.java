package com.stylemycloset.recommendation.mapper;

import com.stylemycloset.clothes.entity.clothes.Clothes;
import com.stylemycloset.recommendation.dto.RecommendationDto;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.weather.entity.Weather;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecommendationMapper {

  private final ClothesMapper clothesMapper;

  public RecommendationDto parseToRecommendationDto(List<Clothes> clothes, Weather weather,
      User user) {
    return new RecommendationDto(
        weather.getId(),
        user.getId(),
        new ArrayList<>(clothes.stream()
            .map(clothesMapper::toClothesDto)
            .toList())
    );


  }
}

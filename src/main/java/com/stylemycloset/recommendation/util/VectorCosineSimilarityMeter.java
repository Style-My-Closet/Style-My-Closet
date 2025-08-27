package com.stylemycloset.recommendation.util;

import com.stylemycloset.clothes.entity.clothes.Clothes;
import com.stylemycloset.clothes.entity.clothes.ClothesAttributeSelectedValue;
import com.stylemycloset.recommendation.entity.ClothingCondition;
import com.stylemycloset.recommendation.mapper.ClothingConditionMapper;
import com.stylemycloset.recommendation.repository.ClothingConditionRepositoryCustom;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.weather.entity.Weather;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VectorCosineSimilarityMeter {

  private final ClothingConditionRepositoryCustom repository;
  private final ClothingConditionMapper clothingConditionMapper;

  public boolean recommend(Clothes cloth, Weather weather, User user) {

    ClothingCondition cc =
        clothingConditionMapper.from3Entity(cloth.getSelectedValues(), weather, user, false);

    float[] inputVector = cc.getEmbedding();

    ClothingCondition mostSimilar = repository.findMostSimilarByVector(inputVector);

    return mostSimilar != null && Boolean.TRUE.equals(mostSimilar.getLabel());
  }

  // 사용자 피드백 데이터 저장
  public void recordFeedback(Weather weather, User user, List<ClothesAttributeSelectedValue> values,
      Boolean label) {
    ClothingCondition feature = clothingConditionMapper.from3Entity(values, weather, user, label);
    repository.saveIfNotDuplicate(feature);
  }
}

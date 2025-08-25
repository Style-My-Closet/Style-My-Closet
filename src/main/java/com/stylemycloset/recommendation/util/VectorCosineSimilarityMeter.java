package com.stylemycloset.recommendation.util;

import com.stylemycloset.clothes.entity.clothes.Clothes;
import com.stylemycloset.clothes.entity.clothes.ClothesAttributeSelectedValue;
import com.stylemycloset.recommendation.entity.ClothingCondition;
import com.stylemycloset.recommendation.mapper.ClothingConditionMapper;
import com.stylemycloset.recommendation.repository.ClothingConditionRepository;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.weather.entity.Weather;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VectorCosineSimilarityMeter {

  private final ClothingConditionRepository repository;
  private final ConditionVectorizer conditionVectorizer;
  private final ClothingConditionMapper clothingConditionMapper;

  public boolean recommend(Clothes cloth, Weather weather, User user) {

    float[] inputVector = conditionVectorizer.toConditionVector(
        clothingConditionMapper.from3Entity(cloth.getSelectedValues(), weather, user, false)
    );

    ClothingCondition mostSimilar = repository.findMostSimilar(inputVector);

    return mostSimilar != null && Boolean.TRUE.equals(mostSimilar.getLabel());
  }

  // 사용자 피드백 데이터 저장
  public void recordFeedback(Weather weather, User user, List<ClothesAttributeSelectedValue> values,
      Boolean label) {
    ClothingCondition feature = clothingConditionMapper.from3Entity(values, weather, user, label);
    repository.save(feature);
  }
}

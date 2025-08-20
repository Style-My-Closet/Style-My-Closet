package com.stylemycloset.recommendation.util;


import com.stylemycloset.cloth.entity.ClothingAttributeValue;
import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import com.stylemycloset.recommendation.dto.RecommendationDto;
import com.stylemycloset.recommendation.entity.ClothingCondition;
import com.stylemycloset.recommendation.mapper.ClothingConditionMapper;
import com.stylemycloset.recommendation.repository.ClothingConditionRepository;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import com.stylemycloset.weather.entity.Weather;
import com.stylemycloset.weather.repository.WeatherRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VectorCosineSimilarityMeter {

    private final ClothingConditionRepository repository;
    private final WeatherRepository weatherRepository;
    private final UserRepository userRepository;
    private final ConditionVectorizer conditionVectorizer;


    public boolean recommend(RecommendationDto dto) {
        Weather weather = weatherRepository.findById(dto.weatherId())
            .orElseThrow(() -> new StyleMyClosetException(ErrorCode.ERROR_CODE, Map.of("weather", "null")));
        User user = userRepository.findById(dto.userID())
            .orElseThrow(() -> new StyleMyClosetException(ErrorCode.ERROR_CODE, Map.of("user", "null")));

        float[] inputVector = conditionVectorizer.toConditionVector(
            ClothingConditionMapper.fromRecommendationDto(dto, weather, user)
        );


        ClothingCondition mostSimilar = repository.findMostSimilar(inputVector);

        return mostSimilar != null && Boolean.TRUE.equals(mostSimilar.getLabel());
    }

    // 사용자 피드백 데이터 저장
    public void recordFeedback(Weather weather, User user, List<ClothingAttributeValue> values, Boolean label) {

        ClothingCondition.ClothingConditionBuilder builder = ClothingCondition.builder()
            .temperature(weather.getTemperature().getCurrent())
            .humidity(weather.getHumidity().getCurrent())
            .weatherType(weather.getAlertType())
            .gender(user.getGender())
            .temperatureSensitivity(user.getTemperatureSensitivity())
            .label(label);

        ClothingCondition.ClothingConditionBuilder builder2 =
            ClothingConditionBuilderHelper.addClothingAttributes(builder,values);

        ClothingCondition feature = builder2.build();

        float[] embedding = conditionVectorizer.toConditionVector(feature);

        feature = builder.embedding(embedding).build();

        repository.save(feature);
    }
}

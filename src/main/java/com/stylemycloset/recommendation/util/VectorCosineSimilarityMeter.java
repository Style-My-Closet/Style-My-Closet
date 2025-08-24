package com.stylemycloset.recommendation.util;


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

    // 코사인 유사도 계산
    private double cosineSimilarity(float[] v1, float[] v2) {
        double dot = 0.0, norm1 = 0.0, norm2 = 0.0;
        for (int i = 0; i < v1.length; i++) {
            dot += v1[i] * v2[i];
            norm1 += Math.pow(v1[i], 2);
            norm2 += Math.pow(v2[i], 2);
        }
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    // 추천 실행
    public boolean recommend(RecommendationDto dto) {
        List<ClothingCondition> features = repository.findAll();

        if (features.isEmpty()) {
            return false; // 데이터 없으면 기본 false
        }


        Weather weather =weatherRepository.findById(dto.weatherId()).orElseThrow(
            ()->new StyleMyClosetException(ErrorCode.ERROR_CODE, Map.of("weather","null"))
        );
        User user = userRepository.findById(dto.userID()).orElseThrow(
            ()->new StyleMyClosetException(ErrorCode.ERROR_CODE, Map.of("user","null"))
        );


        float[] inputVector =  conditionVectorizer.toConditionVector(ClothingConditionMapper.fromRecommendationDto
            (dto, weather, user));

        ClothingCondition mostSimilar = null;
        double highestSimilarity = -1;

        for (ClothingCondition f : features) {
            float[] dbVector = conditionVectorizer.toConditionVector(f);
            double similarity = cosineSimilarity(inputVector, dbVector);
            if (similarity > highestSimilarity) {
                highestSimilarity = similarity;
                mostSimilar = f;
            }
        }

        return mostSimilar != null && mostSimilar.getLabel();
    }

    // 사용자 피드백 데이터 저장
    public void recordFeedback(Weather weather, User user, Boolean label) {
        ClothingCondition feature = ClothingCondition.builder()
            .temperature(weather.getTemperature().getCurrent())
            .humidity(weather.getHumidity().getCurrent())
            .weatherType(weather.getAlertType())
            .gender(user.getGender())
            .temperatureSensitivity(user.getTemperatureSensitivity())
            .label(label)
            .build();
        repository.save(feature);
    }
}

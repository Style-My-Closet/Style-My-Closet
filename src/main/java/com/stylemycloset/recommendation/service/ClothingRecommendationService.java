package com.stylemycloset.recommendation.service;

import com.stylemycloset.cloth.entity.ClothingAttribute;
import com.stylemycloset.recommendation.entity.ClothingFeature;
import com.stylemycloset.recommendation.repository.ClothingFeatureRepository;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.weather.entity.Weather;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ClothingRecommendationService {

    private final ClothingFeatureRepository repository;

    /*
    // 간단한 학습 데이터 (온도, 습도, 소재코드, 날씨코드, 추천여부)
    private final List<double[]> featureVectors = List.of(
        new double[]{25, 60, 0, 0, 1}, // cotton, sunny, 추천
        new double[]{18, 80, 1, 1, 0}, // wool, rainy, 비추천
        new double[]{30, 50, 2, 0, 1}, // linen, sunny, 추천
        new double[]{10, 90, 1, 1, 0}, // wool, rainy, 비추천
        new double[]{22, 65, 0, 2, 1}  // cotton, cloudy, 추천
    );*/

    // 코사인 유사도 계산
    private double cosineSimilarity(double[] v1, double[] v2) {
        double dot = 0.0, norm1 = 0.0, norm2 = 0.0;
        for (int i = 0; i < v1.length; i++) {
            dot += v1[i] * v2[i];
            norm1 += Math.pow(v1[i], 2);
            norm2 += Math.pow(v2[i], 2);
        }
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    // 추천 실행
    public boolean recommend(double temperature, double humidity, String material, String weatherType) {
        List<ClothingFeature> features = repository.findAll();

        if (features.isEmpty()) {
            return false; // 데이터 없으면 기본 false
        }

        // material/weatherType을 단순 코드화
        double materialCode = material.hashCode() % 1000; // 간단한 매핑 예시
        double weatherCode = weatherType.hashCode() % 1000;

        double[] inputVector = {temperature, humidity, materialCode, weatherCode};

        ClothingFeature mostSimilar = null;
        double highestSimilarity = -1;

        for (ClothingFeature f : features) {
            double[] dbVector = {
                f.getTemperature(),
                f.getHumidity(),
                f.getWindSpeed(),
                f.getTemperatureSensitivity(),
                f.getWeatherType().hashCode() % 1000,
                f.getGender().hashCode() % 1000
            };
            double similarity = cosineSimilarity(inputVector, dbVector);
            if (similarity > highestSimilarity) {
                highestSimilarity = similarity;
                mostSimilar = f;
            }
        }

        return mostSimilar != null && mostSimilar.getLabel();
    }

    public void parseToVector(ClothingFeature clothingFeature) {

    }

    // 사용자 피드백 데이터 저장
    public void recordFeedback(Weather weather, User user, ClothingAttribute clothingAttribute, Boolean label) {
        ClothingFeature feature = ClothingFeature.builder()
            .temperature(weather.getTemperature().getCurrent())
            .humidity(weather.getHumidity().getCurrent())
            .weatherType(weather.getAlertType().toString())
            .gender(user.getGender())
            .temperatureSensitivity(user.getTemperatureSensitivity())
            .label(label)
            .build();
        repository.save(feature);
    }
}

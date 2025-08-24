package com.stylemycloset.recommendation.service;

import com.stylemycloset.clothes.entity.clothes.Clothes;
import com.stylemycloset.clothes.repository.clothes.ClothesRepository;
import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import com.stylemycloset.recommendation.dto.RecommendationDto;
import com.stylemycloset.recommendation.util.VectorCosineSimilarityMeter;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.weather.entity.Weather;
import com.stylemycloset.weather.repository.WeatherRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClothingRecommendationService {
    WeatherRepository weatherRepository;
    ClothesRepository clothRepository;
    VectorCosineSimilarityMeter vectorCosineSimilarityMeter;

    public RecommendationDto recommendation(Long weatherId) {
        //CustomUserDetail 구현 될시 user 가져오기
        User dummyUser = null; // 일단 임시로

        Weather weather =weatherRepository.findById(weatherId).orElseThrow(
            ()->new StyleMyClosetException(ErrorCode.ERROR_CODE, Map.of("weather","null"))
        );

        List<Clothes> clothes = clothRepository.findAll();

        RecommendationDto result = null;
        RecommendationDto current = parseToRecommendationDto(clothes,weather,dummyUser);

        for(Clothes c :clothes){
            if(vectorCosineSimilarityMeter.recommend(current)){
                result = current;
                vectorCosineSimilarityMeter.recordFeedback(weather,dummyUser,true);
            } else continue;
        }

        return result;
    }

   private RecommendationDto parseToRecommendationDto(List<Clothes> clothes, Weather weather ,User user) {
        return new RecommendationDto(
            weather.getId(),
            user.getId(),
            clothes
        );
   }
}

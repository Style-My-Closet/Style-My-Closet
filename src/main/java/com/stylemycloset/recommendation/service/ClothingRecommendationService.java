package com.stylemycloset.recommendation.service;

import com.stylemycloset.cloth.entity.Cloth;
import com.stylemycloset.cloth.entity.ClothingAttribute;
import com.stylemycloset.cloth.repository.ClothRepository;
import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import com.stylemycloset.recommendation.dto.RecommendationDto;
import com.stylemycloset.recommendation.entity.ClothingFeature;

import com.stylemycloset.recommendation.util.VectorCosineSimilarityMeter;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import com.stylemycloset.weather.entity.Weather;
import com.stylemycloset.weather.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ClothingRecommendationService {
    WeatherRepository weatherRepository;
    ClothRepository clothRepository;
    VectorCosineSimilarityMeter vectorCosineSimilarityMeter;

    public RecommendationDto recommendation(Long weatherId) {
        //CustomUserDetail 구현 될시 user 가져오기
        User dummyUser = null; // 일단 임시로

        Weather weather =weatherRepository.findById(weatherId).orElseThrow(
            ()->new StyleMyClosetException(ErrorCode.ERROR_CODE, Map.of("weather","null"))
        );

        List<Cloth> clothes = clothRepository.findAll();

        RecommendationDto result = null;
        RecommendationDto current = parseToRecommendationDto(clothes,weather,dummyUser);

        for(Cloth c :clothes){
            if(vectorCosineSimilarityMeter.recommend(current)){
                result = current;
                vectorCosineSimilarityMeter.recordFeedback(weather,dummyUser,c.getAttributeValues(),true);
            } else continue;
        }

        return result;
    }

   private RecommendationDto parseToRecommendationDto(List<Cloth> clothes, Weather weather ,User user) {
        return new RecommendationDto(
            weather.getId(),
            user.getId(),
            clothes
        );
   }
}

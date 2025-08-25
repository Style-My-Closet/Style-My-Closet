package com.stylemycloset.recommendation.service;

import static com.stylemycloset.recommendation.mapper.RecommendationMapper.parseToRecommendationDto;

import com.stylemycloset.cloth.entity.Cloth;
import com.stylemycloset.cloth.repository.ClothRepository;
import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import com.stylemycloset.recommendation.dto.RecommendationDto;

import com.stylemycloset.recommendation.mapper.ClothesMapper;
import com.stylemycloset.recommendation.util.VectorCosineSimilarityMeter;
import com.stylemycloset.security.ClosetUserDetails;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import com.stylemycloset.weather.entity.Weather;
import com.stylemycloset.weather.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final WeatherRepository weatherRepository;
    private final ClothRepository clothRepository;
    private final UserRepository userRepository;
    private final VectorCosineSimilarityMeter vectorCosineSimilarityMeter;
    private final MLModelService mlModelService;

    public RecommendationDto recommendation(Long weatherId) throws XGBoostError {
        ClosetUserDetails userDetails = getCurrentUser();
        User user = null;
        if(userDetails != null) {
            user = userRepository.findById(userDetails.getUserId()).orElseThrow(
                () -> new StyleMyClosetException(ErrorCode.USER_NOT_FOUND)
            ) ;
        }else return new RecommendationDto(0L,0L,null);


        Weather weather =weatherRepository.findById(weatherId).orElseThrow(
            ()->new StyleMyClosetException(ErrorCode.ERROR_CODE, Map.of("weather","null"))
        );

        List<Cloth> clothes = clothRepository.findAll();


        RecommendationDto result = new RecommendationDto(weatherId , user.getId(), new ArrayList<>());
        RecommendationDto current = parseToRecommendationDto(clothes,weather,user);

        if(clothes.size()<10) {
            for(Cloth c :clothes){
                if(!vectorCosineSimilarityMeter.recommend(c, weather, user)) {
                    current.clothes().remove(ClothesMapper.toClothesDto(c));
                    result = current;
                    vectorCosineSimilarityMeter.recordFeedback(weather,user,c.getAttributeValues(),false);
                } else {
                    vectorCosineSimilarityMeter.recordFeedback(weather,user,c.getAttributeValues(),true);
                }
            }
        }else result = mlModelService.prediction(clothes,weather,user);

        return result;
    }



    public ClosetUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null; // 로그인 안 된 상태
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof ClosetUserDetails) {
            return (ClosetUserDetails) principal;
        }

        return null;
    }
}

package com.stylemycloset.recommendation.service;

import com.stylemycloset.clothes.entity.clothes.Clothes;
import com.stylemycloset.clothes.repository.clothes.ClothesRepository;
import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import com.stylemycloset.recommendation.dto.RecommendationDto;
import com.stylemycloset.recommendation.mapper.RecommendationMapper;
import com.stylemycloset.recommendation.util.VectorCosineSimilarityMeter;
import com.stylemycloset.security.ClosetUserDetails;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import com.stylemycloset.weather.entity.Weather;
import com.stylemycloset.weather.repository.WeatherRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecommendationService {

  private final WeatherRepository weatherRepository;
  private final ClothesRepository clothRepository;
  private final UserRepository userRepository;
  private final VectorCosineSimilarityMeter vectorCosineSimilarityMeter;
  private final MLModelService mlModelService;
  private final RecommendationMapper recommendationMapper;

  @Transactional
  public RecommendationDto recommendation(Long weatherId) throws XGBoostError {
    ClosetUserDetails userDetails = getCurrentUser();
    User user = null;
    if (userDetails != null) {
      user = userRepository.findById(userDetails.getUserId()).orElseThrow(
          () -> new StyleMyClosetException(ErrorCode.USER_NOT_FOUND)
      );
    } else {
      return new RecommendationDto(0L, 0L, null);
    }

    Weather weather = weatherRepository.findById(weatherId).orElseThrow(
        () -> new StyleMyClosetException(ErrorCode.ERROR_CODE, Map.of("weather", "null"))
    );

    List<Clothes> clothes = clothRepository.findAllByOwnerIdFetch(user.getId());

    RecommendationDto result = new RecommendationDto(weatherId, user.getId(), new ArrayList<>());
    RecommendationDto current = recommendationMapper.parseToRecommendationDto(clothes, weather,
        user);

    if (clothes.size() < 10) {
      for (Clothes c : clothes) {
        if (!vectorCosineSimilarityMeter.recommend(c, weather, user)) {
          current.clothes().removeIf(dto -> Objects.equals(dto.clothesId(), c.getId()));
          result = current;
          if(!c.getSelectedValues().isEmpty()) {vectorCosineSimilarityMeter.recordFeedback(weather, user, c.getSelectedValues(), false);}
        } else {
          result = current;
          vectorCosineSimilarityMeter.recordFeedback(weather, user, c.getSelectedValues(), true);
        }
      }
    } else {
      result = mlModelService.prediction(clothes, weather, user);
    }

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

package com.stylemycloset.recommendation.controller;



import com.stylemycloset.recommendation.dto.RecommendationDto;
import com.stylemycloset.recommendation.service.MLModelService;
import com.stylemycloset.recommendation.service.RecommendationService;
import com.stylemycloset.weather.entity.Weather;
import lombok.RequiredArgsConstructor;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    
    @GetMapping
    public ResponseEntity<RecommendationDto> getRecommendations(
        @RequestParam Long weatherId
    )  {
        RecommendationDto recommendation = recommendationService.recommendation(weatherId);

        return ResponseEntity.ok(recommendation);
    }
}

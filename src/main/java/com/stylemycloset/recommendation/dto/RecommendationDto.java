package com.stylemycloset.recommendation.dto;

import com.stylemycloset.clothes.entity.clothes.Clothes;
import java.util.List;

public record RecommendationDto(
    Long weatherId,
    Long userID,
    List<Clothes> clothes
) {

}

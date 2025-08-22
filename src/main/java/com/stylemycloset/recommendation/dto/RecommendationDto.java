package com.stylemycloset.recommendation.dto;

import java.util.List;

public record RecommendationDto(
    Long weatherId,
    Long userID,
    List<ClothesDto> clothes
) {

}

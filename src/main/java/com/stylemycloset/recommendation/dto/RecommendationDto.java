package com.stylemycloset.recommendation.dto;

import com.stylemycloset.cloth.entity.Cloth;
import java.util.List;

public record RecommendationDto(
    Long weatherId,
    Long userID,
    List<Cloth> clothes
) {

}

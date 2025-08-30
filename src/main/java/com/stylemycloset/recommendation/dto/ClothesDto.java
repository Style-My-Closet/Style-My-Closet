package com.stylemycloset.recommendation.dto;

import java.util.List;

public record ClothesDto(
    Long clothesId,
    String name,
    String imageUrl,
    String type,
    List<ClothesAttributeWithDefDto> attributes
) {

}

package com.stylemycloset.recommendation.dto;

import java.util.List;

public record ClothesDto(
    Long id,

    String name,
    String imageUrl,
    String type,
    List<ClothesAttributeWithDefDto> attributes
) {

}

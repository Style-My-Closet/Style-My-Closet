package com.stylemycloset.ootd.dto;

import com.stylemycloset.cloth.entity.ClothingCategoryType;
import java.util.List;

public record OotdItemDto(
    Long id,
    String name,
    String imageUrl,
    ClothingCategoryType type,
    List<ClothesAttributeWithDefDto> attributes
) {

}
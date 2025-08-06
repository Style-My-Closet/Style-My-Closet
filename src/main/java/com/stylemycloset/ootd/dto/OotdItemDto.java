package com.stylemycloset.ootd.dto;

import com.stylemycloset.ootd.tempEnum.ClothesType;
import java.util.List;

public record OotdItemDto(
    Long clothesId,
    String name,
    String imageUrl,
    ClothesType type,
    List<ClothesAttributeWithDefDto> attributes
) {

}
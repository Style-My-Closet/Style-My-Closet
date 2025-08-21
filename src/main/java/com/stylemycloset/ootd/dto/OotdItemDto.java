package com.stylemycloset.ootd.dto;

import java.util.List;

public record OotdItemDto(
    Long clothesId,
    String name,
    String imageUrl,
    String type,
    List<ClothesAttributeWithDefinitionDto> attributes
) {

}
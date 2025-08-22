package com.stylemycloset.clothes.dto.clothes;

import com.stylemycloset.clothes.entity.clothes.ClothesType;
import java.util.List;

public record ClothesDto(
    Long id,
    Long ownerId,
    String name,
    String imageUrl,
    ClothesType type,
    List<AttributeDto> attributes
) {

  public static ClothesDto of(
      String name,
      String imageUrl
  ) {
    return new ClothesDto(
        null,
        null,
        name,
        imageUrl,
        null,
        null
    );
  }

}
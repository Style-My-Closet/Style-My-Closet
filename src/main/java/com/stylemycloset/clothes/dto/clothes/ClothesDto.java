package com.stylemycloset.clothes.dto.clothes;

import com.stylemycloset.clothes.dto.ClothesExtractedMetaInfo;
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
      ClothesExtractedMetaInfo metaInfo
  ) {
    return new ClothesDto(
        null,
        null,
        metaInfo.productName(),
        metaInfo.imageUrl(),
        null,
        null
    );
  }

}
package com.stylemycloset.clothes.dto.clothes.response;

import com.stylemycloset.clothes.entity.clothes.Clothes;
import com.stylemycloset.clothes.dto.clothes.AttributeDto;

import com.stylemycloset.clothes.entity.clothes.ClothesType;
import java.util.List;

public record ClothUpdateResponseDto(
    Long id,
    Long ownerId,
    String name,
    String imageUrl,
    ClothesType category,
    List<AttributeDto> attributes

) {

  public static ClothUpdateResponseDto from(Clothes clothes) {
    List<AttributeDto> attributes = clothes.getSelectedValues()
        .stream()
        .map(AttributeDto::from)
        .toList();
    return new ClothUpdateResponseDto(
        clothes.getId(),
        clothes.getOwnerId(),
        clothes.getName(),
        null,
        clothes.getClothesType(),
        attributes
    );
  }

}

package com.stylemycloset.recommendation.mapper;

import com.stylemycloset.cloth.entity.Cloth;
import com.stylemycloset.cloth.entity.ClothingAttribute;
import com.stylemycloset.recommendation.dto.ClothesAttributeWithDefDto;
import com.stylemycloset.recommendation.dto.ClothesDto;

public class ClothesMapper {
    public static ClothesDto toClothesDto(Cloth cloth){
        return new ClothesDto(
          cloth.getId(),
          cloth.getName(),
          cloth.getBinaryContent().getOriginalFileName(),
          cloth.getCategory().toString(),
          cloth.getAttributeValues().stream()
              .map(ca -> {return toClothesAttributeWithDefDto(ca.getAttribute());})
              .toList()
        );
    }

    private static ClothesAttributeWithDefDto toClothesAttributeWithDefDto(ClothingAttribute ca){
        return new ClothesAttributeWithDefDto(
            ca.getId(),
            ca.getName(),
            ca.getAttributeValues().stream().map(Object::toString).toList(),
            ca.getName()
        );
    }

}

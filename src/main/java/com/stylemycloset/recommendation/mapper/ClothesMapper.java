package com.stylemycloset.recommendation.mapper;

import com.stylemycloset.binarycontent.mapper.BinaryContentMapper;
import com.stylemycloset.clothes.entity.attribute.ClothesAttributeDefinition;
import com.stylemycloset.clothes.entity.attribute.ClothesAttributeSelectableValue;
import com.stylemycloset.clothes.entity.clothes.Clothes;
import com.stylemycloset.recommendation.dto.ClothesAttributeWithDefDto;
import com.stylemycloset.recommendation.dto.ClothesDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("recommendationClothesMapper")
@RequiredArgsConstructor
public class ClothesMapper {

  private final BinaryContentMapper binaryContentMapper;

  public ClothesDto toClothesDto(Clothes cloth) {
    return new ClothesDto(
        cloth.getId(),
        cloth.getName(),
        binaryContentMapper.extractUrl(cloth.getImage()),
        cloth.getClothesType().toString(),
        cloth.getSelectedValues().stream()
            .map(ca -> toClothesAttributeWithDefDto(ca.getSelectableValue().getDefinition(), ca.getSelectableValue()))
            .toList()
    );
  }

  private static ClothesAttributeWithDefDto toClothesAttributeWithDefDto(
      ClothesAttributeDefinition ca, ClothesAttributeSelectableValue cs
  ) {
    return new ClothesAttributeWithDefDto(
        ca.getId(),
        ca.getName(),
        ca.getSelectableValues().stream().map(ClothesAttributeSelectableValue::getValue).toList(),
        cs.getValue()
    );
  }

}

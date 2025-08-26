package com.stylemycloset.clothes.dto.attribute;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stylemycloset.clothes.entity.attribute.ClothesAttributeDefinition;
import com.stylemycloset.clothes.entity.attribute.ClothesAttributeSelectableValue;
import java.util.List;

public record ClothesAttributeDefinitionDto(
    Long id,
    @JsonProperty(value = "name")
    String definitionName,
    List<String> selectableValues
) {

  public static ClothesAttributeDefinitionDto from(ClothesAttributeDefinition attribute) {
    List<String> selectableValues = attribute.getSelectableValues()
        .stream()
        .map(ClothesAttributeSelectableValue::getValue)
        .toList();

    return new ClothesAttributeDefinitionDto(
        attribute.getId(),
        attribute.getName(),
        selectableValues
    );
  }

} 
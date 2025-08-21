package com.stylemycloset.clothes.dto.clothes;

import com.stylemycloset.clothes.entity.attribute.ClothesAttributeSelectedValue;
import com.stylemycloset.clothes.entity.attribute.ClothesAttributeDefinition;
import com.stylemycloset.clothes.entity.attribute.ClothesAttributeSelectableValue;
import java.util.List;

public record AttributeDto(
    Long definitionId,
    String definitionName,
    List<String> selectableValues,
    String value
) {

  public static AttributeDto from( // TODO: 8/17/25 N+1 처리필요
      ClothesAttributeSelectedValue attributeSelectedValue
  ) {
    ClothesAttributeSelectableValue selectedValue = attributeSelectedValue.getSelectableValue();
    ClothesAttributeDefinition definition = selectedValue.getDefinition();
    return new AttributeDto(
        definition.getId(),
        definition.getName(),
        getSelectableValues(definition),
        selectedValue.getValue()
    );
  }

  private static List<String> getSelectableValues(ClothesAttributeDefinition definition) {
    return definition.getSelectableValues()
        .stream()
        .map(ClothesAttributeSelectableValue::getValue)
        .toList();
  }

} 
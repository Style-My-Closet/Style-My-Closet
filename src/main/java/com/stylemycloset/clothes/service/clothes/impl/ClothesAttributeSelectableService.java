package com.stylemycloset.clothes.service.clothes.impl;

import com.stylemycloset.clothes.dto.attribute.request.AttributeRequestDto;
import com.stylemycloset.clothes.entity.attribute.ClothesAttributeSelectableValue;
import com.stylemycloset.clothes.repository.attribute.ClothesAttributeDefinitionSelectableRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClothesAttributeSelectableService {

  private final ClothesAttributeDefinitionSelectableRepository definitionSelectableValueRepository;

  public List<ClothesAttributeSelectableValue> getSelectableValues(
      List<AttributeRequestDto> selectedValueRequests
  ) {
    if (selectedValueRequests == null || selectedValueRequests.isEmpty()) {
      return null;
    }
    return selectedValueRequests.stream()
        .map(this::getClothesDefinitionSelectableValue)
        .filter(Objects::nonNull)
        .toList();
  }

  private ClothesAttributeSelectableValue getClothesDefinitionSelectableValue(
      AttributeRequestDto attribute
  ) {
    if (attribute == null) {
      return null;
    }
    return definitionSelectableValueRepository.findByDefinitionIdAndValue(
            attribute.definitionId(),
            attribute.value()
        )
        .orElse(null);
  }

}

package com.stylemycloset.clothes.service.attribute;

import com.stylemycloset.clothes.dto.attribute.ClothesAttributeDefinitionDto;
import com.stylemycloset.clothes.dto.attribute.ClothesAttributeDefinitionDtoCursorResponse;
import com.stylemycloset.clothes.dto.attribute.request.ClothesAttributeCreateRequest;
import com.stylemycloset.clothes.dto.attribute.request.ClothesAttributeSearchCondition;
import com.stylemycloset.clothes.dto.attribute.request.ClothesAttributeUpdateRequest;

public interface ClothAttributeService {

  ClothesAttributeDefinitionDto createAttribute(ClothesAttributeCreateRequest request);

  ClothesAttributeDefinitionDtoCursorResponse getAttributes(
      ClothesAttributeSearchCondition searchCondition
  );

  ClothesAttributeDefinitionDto updateAttribute(
      Long definitionId,
      ClothesAttributeUpdateRequest request
  );

  void softDeleteAttributeById(Long definitionId);

  void deleteAttributeById(Long definitionId);

}

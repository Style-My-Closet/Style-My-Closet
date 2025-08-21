package com.stylemycloset.clothes.service.attribute;

import com.stylemycloset.clothes.dto.attribute.ClothesAttributeDefinitionDtoCursorResponse;
import com.stylemycloset.clothes.dto.attribute.ClothesAttributeDefinitionDto;
import com.stylemycloset.clothes.dto.attribute.request.ClothesAttributeDefinitionCreateRequest;
import com.stylemycloset.clothes.dto.attribute.request.ClothesAttributeDefinitionUpdateRequest;
import com.stylemycloset.clothes.dto.attribute.request.ClothesAttributeSearchCondition;

public interface ClothAttributeService {

  ClothesAttributeDefinitionDto createAttribute(ClothesAttributeDefinitionCreateRequest request);

  ClothesAttributeDefinitionDtoCursorResponse getAttributes(
      ClothesAttributeSearchCondition searchCondition
  );

  ClothesAttributeDefinitionDto updateAttribute(
      Long definitionId,
      ClothesAttributeDefinitionUpdateRequest request
  );

  void deleteAttributeById(Long definitionId);

}

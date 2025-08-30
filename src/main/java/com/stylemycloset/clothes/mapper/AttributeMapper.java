package com.stylemycloset.clothes.mapper;

import com.stylemycloset.clothes.dto.attribute.ClothesAttributeDefinitionDto;
import com.stylemycloset.clothes.dto.attribute.ClothesAttributeDefinitionDtoCursorResponse;
import com.stylemycloset.clothes.entity.attribute.ClothesAttributeDefinition;
import com.stylemycloset.common.repository.CustomSliceImpl;
import com.stylemycloset.common.repository.NextCursorInfo;
import java.util.List;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;

@Component
public class AttributeMapper {

  public ClothesAttributeDefinitionDtoCursorResponse toPageResponse(
      Slice<ClothesAttributeDefinition> attributeDefinitions
  ) {
    List<ClothesAttributeDefinitionDto> definitions = getDefinitions(attributeDefinitions);
    Order order = CustomSliceImpl.getOrder(attributeDefinitions);
    NextCursorInfo nextCursorInfo = NextCursorInfo.attributeDefinitionCursor(attributeDefinitions,
        order.getProperty());

    return ClothesAttributeDefinitionDtoCursorResponse.of(
        definitions,
        nextCursorInfo.nextCursor(),
        nextCursorInfo.nextIdAfter(),
        attributeDefinitions.hasNext(),
        null,
        order.getProperty(),
        order.getDirection().toString()
    );
  }

  private List<ClothesAttributeDefinitionDto> getDefinitions(
      Slice<ClothesAttributeDefinition> attributeDefinitions
  ) {
    return attributeDefinitions.getContent()
        .stream()
        .map(ClothesAttributeDefinitionDto::from)
        .toList();
  }

} 
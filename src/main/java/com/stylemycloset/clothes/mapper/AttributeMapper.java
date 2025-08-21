package com.stylemycloset.clothes.mapper;

import com.stylemycloset.clothes.dto.attribute.ClothesAttributeDefinitionDto;
import com.stylemycloset.clothes.dto.attribute.ClothesAttributeDefinitionDtoCursorResponse;
import com.stylemycloset.clothes.entity.attribute.ClothesAttributeDefinition;
import com.stylemycloset.clothes.repository.attribute.cursor.ClothesAttributeDefinitionField;
import com.stylemycloset.common.repository.CursorStrategy;
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
    List<ClothesAttributeDefinitionDto> definitions = attributeDefinitions.getContent()
        .stream()
        .map(ClothesAttributeDefinitionDto::from)
        .toList();
    Order order = CustomSliceImpl.getOrder(attributeDefinitions);
    NextCursorInfo nextCursorInfo = extractNextCursorInfo(attributeDefinitions, order);

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

  private NextCursorInfo extractNextCursorInfo(
      Slice<ClothesAttributeDefinition> attributeDefinitions,
      Order order
  ) {
    if (order == null ||
        !attributeDefinitions.hasNext() || attributeDefinitions.getContent().isEmpty()
    ) {
      return new NextCursorInfo(null, null);
    }

    ClothesAttributeDefinition lastDefinition = attributeDefinitions.getContent()
        .get(attributeDefinitions.getContent().size() - 1);
    CursorStrategy<?, ClothesAttributeDefinition> cursorStrategy = ClothesAttributeDefinitionField.resolveStrategy(
        order.getProperty());
    String cursor = cursorStrategy.extract(lastDefinition).toString();
    String idAfter = lastDefinition.getId().toString();

    return new NextCursorInfo(cursor, idAfter);
  }

} 
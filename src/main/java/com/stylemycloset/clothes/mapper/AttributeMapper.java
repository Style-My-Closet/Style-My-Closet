package com.stylemycloset.clothes.mapper;

import com.stylemycloset.clothes.dto.attribute.ClothesAttributeDefinitionDtoCursorResponse;
import com.stylemycloset.clothes.dto.attribute.ClothesAttributeDefinitionDto;
import com.stylemycloset.clothes.entity.attribute.ClothesAttributeDefinition;
import com.stylemycloset.clothes.repository.attribute.cursor.ClothesAttributeDefinitionField;
import com.stylemycloset.common.repository.cursor.CursorStrategy;
import com.stylemycloset.common.repository.cursor.NextCursorInfo;
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
    Order order = getOrder(attributeDefinitions);
    NextCursorInfo nextCursorInfo = extractNextCursorInfo(attributeDefinitions,
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

  private Order getOrder(Slice<ClothesAttributeDefinition> attributeDefinitions) {
    return attributeDefinitions.getPageable()
        .getSort()
        .stream()
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("DTO 변환시 정렬 순서(Order)가 존재하지 않습니다."));
  }

  private NextCursorInfo extractNextCursorInfo(
      Slice<ClothesAttributeDefinition> attributeDefinitions,
      String sortBy
  ) {
    if (sortBy == null || sortBy.isBlank() ||
        !attributeDefinitions.hasNext() || attributeDefinitions.getContent().isEmpty()
    ) {
      return new NextCursorInfo(null, null);
    }

    ClothesAttributeDefinition lastDefinition = attributeDefinitions.getContent()
        .get(attributeDefinitions.getContent().size() - 1);
    CursorStrategy<?, ClothesAttributeDefinition> cursorStrategy = ClothesAttributeDefinitionField.resolveStrategy(
        sortBy);
    String cursor = cursorStrategy.extract(lastDefinition).toString();
    String idAfter = lastDefinition.getId().toString();

    return new NextCursorInfo(cursor, idAfter);
  }

} 
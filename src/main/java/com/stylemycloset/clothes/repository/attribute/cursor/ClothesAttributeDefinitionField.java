package com.stylemycloset.clothes.repository.attribute.cursor;

import static com.stylemycloset.clothes.entity.attribute.QClothesAttributeDefinition.clothesAttributeDefinition;

import com.stylemycloset.clothes.entity.attribute.ClothesAttributeDefinition;
import com.stylemycloset.clothes.repository.attribute.cursor.strategy.AttributeIdCursorStrategy;
import com.stylemycloset.clothes.repository.attribute.cursor.strategy.AttributeTextCursorStrategy;
import com.stylemycloset.clothes.repository.attribute.cursor.strategy.AttributeTimeCursorStrategy;
import com.stylemycloset.common.repository.cursor.CursorStrategy;
import java.time.Instant;
import java.util.Arrays;

public enum ClothesAttributeDefinitionField {

  ID(new AttributeIdCursorStrategy(
      clothesAttributeDefinition.id,
      Long::parseLong,
      ClothesAttributeDefinition::getId)
  ),

  NAME(new AttributeTextCursorStrategy(
      clothesAttributeDefinition.name,
      s -> s,
      ClothesAttributeDefinition::getName)
  ),

  CREATED_AT(new AttributeTimeCursorStrategy(
      clothesAttributeDefinition.createdAt,
      Instant::parse,
      ClothesAttributeDefinition::getCreatedAt)
  );

  private final CursorStrategy<?, ClothesAttributeDefinition> cursorStrategy;

  ClothesAttributeDefinitionField(
      CursorStrategy<?, ClothesAttributeDefinition> cursorStrategy
  ) {
    this.cursorStrategy = cursorStrategy;
  }

  public static CursorStrategy<?, ClothesAttributeDefinition> resolveStrategy(String sortBy) {
    if (sortBy == null || sortBy.isBlank()) {
      return CREATED_AT.cursorStrategy;
    }

    return Arrays.stream(ClothesAttributeDefinitionField.values())
        .filter(field -> isSameName(field, sortBy.trim()))
        .findFirst()
        .map(messageCursorField -> messageCursorField.cursorStrategy)
        .orElseThrow(() -> new IllegalArgumentException(
            ClothesAttributeDefinitionField.class.getName() + "요청하신 정렬 기준 필드명에 맞는 필드명이 없습니다.")
        );
  }

  private static boolean isSameName(ClothesAttributeDefinitionField field, String sortBy) {
    return field.cursorStrategy
        .path()
        .getMetadata()
        .getName()
        .equalsIgnoreCase(sortBy);
  }

}

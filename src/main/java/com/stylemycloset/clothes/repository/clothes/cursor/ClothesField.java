package com.stylemycloset.clothes.repository.clothes.cursor;

import static com.stylemycloset.clothes.entity.clothes.QClothes.clothes;

import com.stylemycloset.clothes.entity.clothes.Clothes;
import com.stylemycloset.clothes.repository.clothes.cursor.strategy.ClothesNumberFieldStrategy;
import com.stylemycloset.clothes.repository.clothes.cursor.strategy.ClothesTimeFieldStrategy;
import com.stylemycloset.common.repository.cursor.CursorStrategy;
import java.time.Instant;
import java.util.Arrays;

public enum ClothesField {

  ID(new ClothesNumberFieldStrategy(
      clothes.id,
      Long::parseLong,
      Clothes::getId)
  ),

  CREATED_AT(new ClothesTimeFieldStrategy(
      clothes.createdAt,
      Instant::parse,
      Clothes::getCreatedAt)
  );

  private final CursorStrategy<?, Clothes> cursorStrategy;

  ClothesField(CursorStrategy<?, Clothes> cursorStrategy) {
    this.cursorStrategy = cursorStrategy;
  }

  public static CursorStrategy<?, Clothes> resolveStrategy(String sortBy) {
    if (sortBy == null || sortBy.isBlank()) {
      return CREATED_AT.cursorStrategy;
    }

    return Arrays.stream(ClothesField.values())
        .filter(field -> isSameName(field, sortBy.trim()))
        .findFirst()
        .map(clothesCursorField -> clothesCursorField.cursorStrategy)
        .orElseThrow(() -> new IllegalArgumentException(
            ClothesField.class.getName() + "요청하신 정렬 기준 필드명에 맞는 필드명이 없습니다.")
        );
  }

  private static boolean isSameName(ClothesField field, String sortBy) {
    return field.cursorStrategy
        .path()
        .getMetadata()
        .getName()
        .equalsIgnoreCase(sortBy);
  }

}

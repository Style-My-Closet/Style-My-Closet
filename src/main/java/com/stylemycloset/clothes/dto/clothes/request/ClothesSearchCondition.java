package com.stylemycloset.clothes.dto.clothes.request;

import com.stylemycloset.clothes.entity.clothes.ClothesType;

public record ClothesSearchCondition(
    String cursor,
    String idAfter,
    Integer limit,
    ClothesType typeEqual,
    Long ownerId
) {

  private static final int DEFAULT_LIMIT = 20;

  public Integer limit() {
    return limit != null ? limit : DEFAULT_LIMIT;
  }

}

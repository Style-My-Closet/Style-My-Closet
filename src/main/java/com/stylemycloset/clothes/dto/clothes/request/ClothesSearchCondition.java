package com.stylemycloset.clothes.dto.clothes.request;

public record ClothesSearchCondition(
    String cursor,
    Long idAfter,
    Integer limit,
    String typeEqual,
    Long ownerId
) {

  private static final int DEFAULT_LIMIT = 20;

  public Integer limit() {
    return limit != null ? limit : DEFAULT_LIMIT;
  }

}

package com.stylemycloset.clothes.dto.clothes.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stylemycloset.clothes.entity.clothes.ClothesType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ClothesSearchCondition(
    @JsonProperty("cursor")
    String cursor,
    @JsonProperty("string")
    String idAfter,
    @NotNull
    @Positive
    Integer limit,
    ClothesType typeEqual,
    @NotNull
    @Positive
    Long ownerId
) {

  private static final int DEFAULT_LIMIT = 20;

  public Integer limit() {
    return limit != null ? limit : DEFAULT_LIMIT;
  }

}

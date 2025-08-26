package com.stylemycloset.clothes.dto.attribute;

import java.util.List;


public record ClothesAttributeDefinitionDtoCursorResponse(
    List<ClothesAttributeDefinitionDto> data,
    String nextCursor,
    String nextIdAfter,
    Boolean hasNext,
    Long totalCount,
    String sortBy,
    String sortDirection
) {

  public static ClothesAttributeDefinitionDtoCursorResponse of(
      List<ClothesAttributeDefinitionDto> data,
      String nextCursor,
      String nextIdAfter,
      Boolean hasNext,
      Long totalCount,
      String sortBy,
      String sortDirection
  ) {
    return new ClothesAttributeDefinitionDtoCursorResponse(
        data,
        nextCursor,
        nextIdAfter,
        hasNext,
        totalCount,
        sortBy,
        sortDirection
    );
  }

} 
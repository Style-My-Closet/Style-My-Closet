package com.stylemycloset.clothes.dto.attribute.request;

public record ClothesAttributeSearchCondition(
    String cursor,
    Long idAfter,
    Integer limit,
    String sortBy,
    String sortDirection,
    String keywordLike
) {

}

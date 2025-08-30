package com.stylemycloset.clothes.dto.attribute.request;

import org.springframework.data.domain.Sort.Direction;

public record ClothesAttributeSearchCondition(
    String cursor,
    String idAfter,
    Integer limit,
    String sortBy,
    Direction sortDirection,
    String keywordLike
) {

}

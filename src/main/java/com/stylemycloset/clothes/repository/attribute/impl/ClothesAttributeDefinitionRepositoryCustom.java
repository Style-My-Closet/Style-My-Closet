package com.stylemycloset.clothes.repository.attribute.impl;

import com.stylemycloset.clothes.entity.attribute.ClothesAttributeDefinition;
import org.springframework.data.domain.Slice;

public interface ClothesAttributeDefinitionRepositoryCustom {

  Slice<ClothesAttributeDefinition> findWithCursorPagination(
      String cursor,
      Long idAfter,
      Integer limit,
      String sortBy,
      String sortDirection,
      String keywordLike
  );

}
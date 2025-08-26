package com.stylemycloset.clothes.repository.attribute.impl;

import com.stylemycloset.clothes.entity.attribute.ClothesAttributeDefinition;
import java.util.Optional;
import org.hibernate.query.SortDirection;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort.Direction;

public interface ClothesAttributeDefinitionRepositoryCustom {

  Slice<ClothesAttributeDefinition> findWithCursorPagination(
      String cursor,
      String idAfter,
      Integer limit,
      String sortBy,
      Direction sortDirection,
      String keywordLike
  );


}
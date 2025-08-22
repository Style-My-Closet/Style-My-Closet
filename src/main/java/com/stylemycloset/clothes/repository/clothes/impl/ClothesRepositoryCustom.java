package com.stylemycloset.clothes.repository.clothes.impl;

import com.stylemycloset.clothes.entity.clothes.Clothes;
import org.springframework.data.domain.Slice;

public interface ClothesRepositoryCustom {

  Slice<Clothes> findClothesByCondition(
      String cursor,
      Long idAfter,
      Integer limit,
      String typeEqual,
      Long ownerId,
      String sortBy,
      String direction
  );

}
package com.stylemycloset.clothes.repository.clothes.impl;

import com.stylemycloset.clothes.entity.clothes.Clothes;
import com.stylemycloset.clothes.entity.clothes.ClothesType;
import java.util.List;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort.Direction;

public interface ClothesRepositoryCustom {

  Slice<Clothes> findClothesByCondition(
      String cursor,
      String idAfter,
      Integer limit,
      ClothesType typeEqual,
      Long ownerId,
      String sortBy,
      Direction direction
  );

  List<Clothes> findAllByOwnerIdFetch(Long ownerId);

}
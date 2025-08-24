package com.stylemycloset.clothes.repository.clothes.impl;

import static com.stylemycloset.clothes.entity.clothes.QClothes.clothes;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stylemycloset.clothes.entity.clothes.Clothes;
import com.stylemycloset.clothes.entity.clothes.ClothesType;
import com.stylemycloset.clothes.repository.clothes.cursor.ClothesField;
import com.stylemycloset.common.repository.CursorStrategy;
import com.stylemycloset.common.repository.CustomSliceImpl;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort.Direction;


@Slf4j
@RequiredArgsConstructor
public class ClothesRepositoryImpl implements ClothesRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Slice<Clothes> findClothesByCondition(
      String cursor,
      String idAfter,
      Integer limit,
      ClothesType typeEqual,
      Long ownerId,
      String sortBy,
      Direction direction
  ) {
    CursorStrategy<?, Clothes> primaryCursorStrategy = ClothesField.resolveStrategy(sortBy);
    CursorStrategy<?, Clothes> idAfterCursorStrategy = ClothesField.resolveStrategy(
        clothes.id.getMetadata().getName());

    List<Clothes> content = queryFactory
        .selectFrom(clothes)
        .join(clothes.image).fetchJoin()
        .where(
            buildTypeEqualPredicate(typeEqual),
            primaryCursorStrategy.buildCursorPredicate(direction, cursor, idAfter,
                idAfterCursorStrategy)
        )
        .orderBy(
            primaryCursorStrategy.buildOrder(direction),
            idAfterCursorStrategy.buildOrder(direction)
        )
        .limit(limit + 1)
        .fetch();

    return CustomSliceImpl.of(content, limit, primaryCursorStrategy, direction);
  }

  private BooleanExpression buildTypeEqualPredicate(ClothesType typeEqual) {
    if (typeEqual == null) {
      return null;
    }
    return clothes.clothesType.eq(typeEqual);
  }

}
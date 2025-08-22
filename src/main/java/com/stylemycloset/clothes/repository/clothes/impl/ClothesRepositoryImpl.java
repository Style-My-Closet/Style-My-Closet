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
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class ClothesRepositoryImpl implements ClothesRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Slice<Clothes> findClothesByCondition(
      String cursor,
      Long idAfter,
      Integer limit,
      String typeEqual,
      Long ownerId,
      String sortBy,
      String direction
  ) {
    CursorStrategy<?, Clothes> primaryCursorStrategy = ClothesField.resolveStrategy(sortBy);
    CursorStrategy<?, Clothes> idAfterCursorStrategy = ClothesField.resolveStrategy(
        clothes.id.getMetadata().getName());

    List<Clothes> content = queryFactory
        .selectFrom(clothes)
        .where(
            buildTypeEqualPredicate(typeEqual),
            buildClothesCursorPredicate(primaryCursorStrategy,
                idAfterCursorStrategy, direction, cursor)
        ).orderBy(
            primaryCursorStrategy.buildOrder(direction, cursor),
            idAfterCursorStrategy.buildOrder(direction, cursor)
        )
        .limit(limit + 1)
        .fetch();

    return CustomSliceImpl.of(content, limit, primaryCursorStrategy, direction);
  }

  private BooleanExpression buildTypeEqualPredicate(String typeEqual) {
    if (typeEqual == null) {
      return null;
    }
    return clothes.clothesType.eq(ClothesType.valueOf(typeEqual));
  }

  private BooleanExpression buildClothesCursorPredicate(
      CursorStrategy<?, Clothes> primaryCursorStrategy,
      CursorStrategy<?, Clothes> idAfterCursorStrategy,
      String direction,
      String cursor
  ) {
    BooleanExpression booleanExpression = primaryCursorStrategy.buildInequalityPredicate(direction,
        cursor);
    BooleanExpression buildEq = primaryCursorStrategy.buildEq(cursor);
    BooleanExpression buildSecondary = idAfterCursorStrategy.buildInequalityPredicate(direction,
        cursor);
    if (buildEq != null && buildSecondary != null) {
      booleanExpression.or(buildEq.and(buildSecondary));
    }

    return booleanExpression;
  }

}
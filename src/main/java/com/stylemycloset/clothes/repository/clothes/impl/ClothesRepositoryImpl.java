package com.stylemycloset.clothes.repository.clothes.impl;

import static com.stylemycloset.clothes.entity.clothes.QClothes.clothes;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stylemycloset.clothes.entity.clothes.Clothes;
import com.stylemycloset.clothes.repository.clothes.cursor.ClothesField;
import com.stylemycloset.common.repository.cursor.CursorStrategy;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
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
      String TypeEqual,
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
            buildClothesCursorPredicate(cursor, direction, primaryCursorStrategy,
                idAfterCursorStrategy)
        ).orderBy(
            primaryCursorStrategy.buildOrder(direction, cursor),
            idAfterCursorStrategy.buildOrder(direction, cursor)
        )
        .limit(limit + 1)
        .fetch();

    return convertToSlice(content, limit, primaryCursorStrategy, direction);
  }

  private BooleanExpression buildClothesCursorPredicate(
      String cursor,
      String direction,
      CursorStrategy<?, Clothes> primaryCursorStrategy,
      CursorStrategy<?, Clothes> idAfterCursorStrategy
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

  private SliceImpl<Clothes> convertToSlice(
      List<Clothes> clothes,
      Integer limit,
      CursorStrategy<?, Clothes> primaryCursorStrategy,
      String sortDirection
  ) {
    Objects.requireNonNull(limit, "limit은 null이 될 수 없습니다");
    Sort sort = Sort.by(
        primaryCursorStrategy.parseDirectionOrDefault(sortDirection),
        primaryCursorStrategy.path().getMetadata().getName()
    );

    return new SliceImpl<>(
        clothes.subList(0, Math.min(clothes.size(), limit)),
        PageRequest.of(0, limit, sort),
        clothes.size() > limit
    );
  }

}
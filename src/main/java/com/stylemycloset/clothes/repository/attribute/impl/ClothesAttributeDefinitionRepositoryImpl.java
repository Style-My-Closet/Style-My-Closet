package com.stylemycloset.clothes.repository.attribute.impl;

import static com.stylemycloset.clothes.entity.attribute.QClothesAttributeDefinition.clothesAttributeDefinition;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stylemycloset.clothes.entity.attribute.ClothesAttributeDefinition;
import com.stylemycloset.clothes.repository.attribute.cursor.ClothesAttributeDefinitionField;
import com.stylemycloset.common.repository.CursorStrategy;
import com.stylemycloset.common.repository.CustomSliceImpl;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class ClothesAttributeDefinitionRepositoryImpl implements
    ClothesAttributeDefinitionRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Slice<ClothesAttributeDefinition> findWithCursorPagination(
      String cursor,
      Long idAfter,
      Integer limit,
      String sortBy,
      String sortDirection,
      String keywordLike
  ) {
    CursorStrategy<?, ClothesAttributeDefinition> primaryCursorStrategy = ClothesAttributeDefinitionField.resolveStrategy(
        sortBy);
    CursorStrategy<?, ClothesAttributeDefinition> idPrimaryCursorStrategy = ClothesAttributeDefinitionField.resolveStrategy(
        clothesAttributeDefinition.id.getMetadata().getName());

    List<ClothesAttributeDefinition> attributeDefinitions = queryFactory
        .selectFrom(clothesAttributeDefinition)
        .where(
            nameContains(keywordLike),
            cursorCondition(primaryCursorStrategy, idPrimaryCursorStrategy, cursor, sortDirection)
        )
        .orderBy(clothesAttributeDefinition.id.asc())
        .limit(limit + 1)
        .fetch();

    return CustomSliceImpl.of(attributeDefinitions, limit, primaryCursorStrategy, sortDirection);
  }

  private BooleanExpression nameContains(String keyword) {
    if (StringUtils.hasText(keyword)) {
      return clothesAttributeDefinition.name.containsIgnoreCase(keyword.trim());
    }
    return null;
  }

  private BooleanExpression cursorCondition(
      CursorStrategy<?, ClothesAttributeDefinition> primaryCursorStrategy,
      CursorStrategy<?, ClothesAttributeDefinition> idAfterStrategy,
      String cursor,
      String direction
  ) {
    BooleanExpression booleanExpression = primaryCursorStrategy.buildInequalityPredicate(direction,
        cursor);
    BooleanExpression buildEq = primaryCursorStrategy.buildEq(cursor);
    BooleanExpression buildSecondary = idAfterStrategy.buildInequalityPredicate(direction,
        cursor);
    if (buildEq != null && buildSecondary != null) {
      booleanExpression.or(buildEq.and(buildSecondary));
    }

    return booleanExpression;
  }

}
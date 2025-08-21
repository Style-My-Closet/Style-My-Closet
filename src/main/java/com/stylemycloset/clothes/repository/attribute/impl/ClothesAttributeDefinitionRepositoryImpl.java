package com.stylemycloset.clothes.repository.attribute.impl;

import static com.stylemycloset.clothes.entity.attribute.QClothesAttributeDefinition.clothesAttributeDefinition;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stylemycloset.clothes.entity.attribute.ClothesAttributeDefinition;
import com.stylemycloset.clothes.repository.attribute.cursor.ClothesAttributeDefinitionField;
import com.stylemycloset.common.repository.cursor.CursorStrategy;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
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

    return convertToSlice(attributeDefinitions, primaryCursorStrategy, limit, sortDirection);
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

  private SliceImpl<ClothesAttributeDefinition> convertToSlice( // TODO: 8/19/25  커스텀 이거 해야함
      List<ClothesAttributeDefinition> attributeDefinitions,
      CursorStrategy<?, ClothesAttributeDefinition> primaryCursorStrategy,
      Integer limit,
      String sortDirection
  ) {
    Objects.requireNonNull(limit, "limit은 null이 될 수 없습니다");
    Sort sort = Sort.by(
        primaryCursorStrategy.parseDirectionOrDefault(sortDirection),
        primaryCursorStrategy.path().getMetadata().getName()
    );

    return new SliceImpl<>(
        attributeDefinitions.subList(0, Math.min(attributeDefinitions.size(), limit)),
        PageRequest.of(0, limit, sort),
        attributeDefinitions.size() > limit
    );
  }

}
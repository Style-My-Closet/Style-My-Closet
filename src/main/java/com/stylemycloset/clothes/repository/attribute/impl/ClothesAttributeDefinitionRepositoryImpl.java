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
import org.springframework.data.domain.Sort.Direction;
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
      String idAfter,
      Integer limit,
      String sortBy,
      Direction direction,
      String keywordLike
  ) {
    CursorStrategy<?, ClothesAttributeDefinition> primaryStrategy = ClothesAttributeDefinitionField.resolveStrategy(
        sortBy);
    CursorStrategy<?, ClothesAttributeDefinition> idAfterStrategy = ClothesAttributeDefinitionField.resolveStrategy(
        clothesAttributeDefinition.id.getMetadata().getName()
    );

    List<ClothesAttributeDefinition> attributeDefinitions = queryFactory
        .selectFrom(clothesAttributeDefinition)
        .where(
            nameContains(keywordLike),
            primaryStrategy.buildCursorPredicate(direction, cursor, idAfter, idAfterStrategy)
        )
        .orderBy(
            primaryStrategy.buildOrder(direction),
            idAfterStrategy.buildOrder(direction)
        )
        .limit(limit + 1)
        .fetch();

    return CustomSliceImpl.of(attributeDefinitions, limit, primaryStrategy, direction);
  }

  private BooleanExpression nameContains(String keyword) {
    if (StringUtils.hasText(keyword)) {
      return clothesAttributeDefinition.name.containsIgnoreCase(keyword.trim());
    }
    return null;
  }

}
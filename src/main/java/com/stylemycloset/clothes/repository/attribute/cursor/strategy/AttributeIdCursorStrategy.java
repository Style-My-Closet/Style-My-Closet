package com.stylemycloset.clothes.repository.attribute.cursor.strategy;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.stylemycloset.clothes.entity.attribute.ClothesAttributeDefinition;
import com.stylemycloset.common.repository.cursor.CursorStrategy;
import java.util.function.Function;
import org.springframework.data.domain.Sort.Direction;

public record AttributeIdCursorStrategy(
    NumberPath<Long> path,
    Function<String, Long> parser,
    Function<ClothesAttributeDefinition, Long> extractor
) implements CursorStrategy<Long, ClothesAttributeDefinition> {

  @Override
  public Long parse(String rawCursor) {
    return parser.apply(rawCursor);
  }

  @Override
  public Long extract(ClothesAttributeDefinition instance) {
    if (instance == null) {
      throw new IllegalArgumentException("값을 추출할 인스턴스 값이 비어있습니다.");
    }
    return extractor.apply(instance);
  }

  @Override
  public BooleanExpression buildInequalityPredicate(String rawDirection, String rawCursor) {
    if (rawCursor == null || rawCursor.isBlank()) {
      return null;
    }
    Direction direction = parseDirectionOrDefault(rawDirection);
    Long parsed = parse(rawCursor);
    if (direction.isDescending()) {
      return path.lt(parsed);
    }
    return path.gt(parsed);
  }

  @Override
  public OrderSpecifier<Long> buildOrder(String rawDirection, String rawCursor) {
    Direction direction = parseDirectionOrDefault(rawDirection);
    if (direction.isDescending()) {
      return new OrderSpecifier<>(Order.DESC, path);
    }
    return new OrderSpecifier<>(Order.ASC, path);
  }

  @Override
  public BooleanExpression buildEq(String rawCursor) {
    if (rawCursor == null || rawCursor.isBlank()) {
      return null;
    }
    Long parsed = parse(rawCursor);
    return path.eq(parsed);
  }

}
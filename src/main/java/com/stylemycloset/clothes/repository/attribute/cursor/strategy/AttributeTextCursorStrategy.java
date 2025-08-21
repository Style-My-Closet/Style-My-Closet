package com.stylemycloset.clothes.repository.attribute.cursor.strategy;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.stylemycloset.clothes.entity.attribute.ClothesAttributeDefinition;
import com.stylemycloset.common.repository.CursorStrategy;
import java.util.function.Function;
import org.springframework.data.domain.Sort.Direction;

public record AttributeTextCursorStrategy(
    StringPath path,
    Function<String, String> parser,
    Function<ClothesAttributeDefinition, String> extractor
) implements CursorStrategy<String, ClothesAttributeDefinition> {

  @Override
  public String parse(String rawCursor) {
    return parser.apply(rawCursor);
  }

  @Override
  public String extract(ClothesAttributeDefinition instance) {
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
    String parsed = parse(rawCursor);
    if (direction.isDescending()) {
      return path.lt(parsed);
    }
    return path.gt(parsed);
  }

  @Override
  public OrderSpecifier<String> buildOrder(String rawDirection, String rawCursor) {
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
    String parsed = parse(rawCursor);
    return path.eq(parsed);
  }

}

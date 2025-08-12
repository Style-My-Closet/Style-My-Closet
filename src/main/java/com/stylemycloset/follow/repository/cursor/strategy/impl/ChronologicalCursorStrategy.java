package com.stylemycloset.follow.repository.cursor.strategy.impl;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import com.stylemycloset.follow.entity.Follow;
import com.stylemycloset.follow.repository.cursor.strategy.CursorStrategy;
import java.time.Instant;
import java.util.function.Function;
import org.springframework.data.domain.Sort.Direction;

public record ChronologicalCursorStrategy(
    DateTimePath<Instant> path,
    Function<String, Instant> parser,
    Function<Follow, Instant> extractor
) implements CursorStrategy<Instant> {

  @Override
  public Instant parse(String rawCursor) {
    return parser.apply(rawCursor);
  }

  @Override
  public Instant extract(Follow follow) {
    if (follow == null) {
      throw new IllegalArgumentException("follow 인스턴스 값이 비어있습니다.");
    }
    return extractor.apply(follow);
  }

  @Override
  public BooleanExpression buildPredicate(String rawDirection, String rawCursor) {
    if (rawCursor == null || rawCursor.isBlank()) {
      return null;
    }

    Direction direction = parseDirectionOrDefault(rawDirection);
    Instant parsed = parse(rawCursor);
    if (direction.isDescending()) {
      return path.lt(parsed);
    }
    return path.gt(parsed);
  }

  @Override
  public OrderSpecifier<Instant> buildOrder(String rawDirection, String rawCursor) {
    Direction direction = parseDirectionOrDefault(rawDirection);
    if (direction.isDescending()) {
      return new OrderSpecifier<>(Order.DESC, path);
    }
    return new OrderSpecifier<>(Order.ASC, path);
  }

}

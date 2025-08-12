package com.stylemycloset.directmessage.repository.cursor.strategy.impl;


import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import com.stylemycloset.directmessage.entity.DirectMessage;
import com.stylemycloset.directmessage.repository.cursor.CursorStrategy;
import java.time.Instant;
import java.util.function.Function;
import org.springframework.data.domain.Sort.Direction;

public record ChronologicalCursorStrategy(
    DateTimePath<Instant> path,
    Function<String, Instant> parser,
    Function<DirectMessage, Instant> extractor
) implements CursorStrategy<Instant> {

  @Override
  public Instant parse(String rawCursor) {
    return parser.apply(rawCursor);
  }

  @Override
  public Instant extract(DirectMessage directMessage) {
    if (directMessage == null) {
      throw new IllegalArgumentException("값을 추출할 인스턴스 값이 비어있습니다.");
    }
    return extractor.apply(directMessage);
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

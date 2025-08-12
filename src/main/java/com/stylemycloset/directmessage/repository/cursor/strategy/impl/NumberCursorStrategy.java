package com.stylemycloset.directmessage.repository.cursor.strategy.impl;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.stylemycloset.directmessage.entity.DirectMessage;
import com.stylemycloset.directmessage.repository.cursor.CursorStrategy;
import java.util.function.Function;
import org.springframework.data.domain.Sort.Direction;

public record NumberCursorStrategy<T extends Number & Comparable<T>>(
    NumberPath<T> path,
    Function<String, T> parser,
    Function<DirectMessage, T> extractor
) implements CursorStrategy<T> {


  @Override
  public T parse(String rawCursor) {
    return parser.apply(rawCursor);
  }

  @Override
  public T extract(DirectMessage instance) {
    if (instance == null) {
      throw new IllegalArgumentException("값을 추출할 인스턴스 값이 비어있습니다.");
    }
    return extractor.apply(instance);
  }

  @Override
  public BooleanExpression buildPredicate(String rawDirection, String rawCursor) {
    if (rawCursor == null || rawCursor.isBlank()) {
      return null;
    }
    Direction direction = parseDirectionOrDefault(rawDirection);
    T parsed = parse(rawCursor);
    if (direction.isDescending()) {
      return path.lt(parsed);
    }
    return path.gt(parsed);
  }

  @Override
  public OrderSpecifier<T> buildOrder(String rawDirection, String rawCursor) {
    Direction direction = parseDirectionOrDefault(rawDirection);
    if (direction.isDescending()) {
      return new OrderSpecifier<>(Order.DESC, path);
    }
    return new OrderSpecifier<>(Order.ASC, path);
  }

}

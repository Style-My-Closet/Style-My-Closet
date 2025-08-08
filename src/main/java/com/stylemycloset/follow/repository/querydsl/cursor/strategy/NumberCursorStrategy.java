package com.stylemycloset.follow.repository.querydsl.cursor.strategy;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.stylemycloset.follow.entity.Follow;
import com.stylemycloset.follow.repository.querydsl.cursor.CursorStrategy;
import java.util.function.Function;
import org.springframework.data.domain.Sort.Direction;

public record NumberCursorStrategy<T extends Number & Comparable<T>>(
    NumberPath<T> path,
    Function<String, T> parser,
    Function<Follow, T> extractor
) implements CursorStrategy<T> {


  @Override
  public T parse(String rawCursor) {
    return parser.apply(rawCursor);
  }

  @Override
  public T extract(Follow instance) {
    if (instance == null) {
      throw new IllegalArgumentException("follow 인스턴스 값이 비어있습니다.");
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

package com.stylemycloset.follow.repository.cursor.strategy;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.stylemycloset.common.repository.CursorStrategy;
import com.stylemycloset.follow.entity.Follow;
import java.util.function.Function;
import org.springframework.data.domain.Sort.Direction;

public record NumberCursorStrategy<T extends Number & Comparable<T>>(
    NumberPath<T> path,
    Function<String, T> parser,
    Function<Follow, T> extractor
) implements CursorStrategy<T, Follow> {

  @Override
  public T parse(String rawCursor) {
    return parser.apply(rawCursor);
  }

  @Override
  public T extract(Follow instance) {
    if (instance == null) {
      throw new IllegalArgumentException("추출할 인스턴스 값이 비어있습니다.");
    }
    return extractor.apply(instance);
  }

  @Override
  public BooleanExpression buildInequalityPredicate(Direction direction, String rawCursor) {
    if (rawCursor == null || rawCursor.isBlank()) {
      return null;
    }
    T parsed = parse(rawCursor);
    if (isDescendingOrDefault(direction)) {
      return path.lt(parsed);
    }
    return path.gt(parsed);
  }

  @Override
  public BooleanExpression buildEq(String rawCursor) {
    if (rawCursor == null || rawCursor.isBlank()) {
      return null;
    }
    T parsed = parse(rawCursor);
    return path.eq(parsed);
  }

}

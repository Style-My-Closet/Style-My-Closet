package com.stylemycloset.directmessage.repository.cursor.strategy;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.stylemycloset.common.repository.CursorStrategy;
import com.stylemycloset.directmessage.entity.DirectMessage;
import java.util.function.Function;
import org.springframework.data.domain.Sort.Direction;

public record DirectMessageNumberCursorStrategy<T extends Number & Comparable<T>>(
    NumberPath<T> path,
    Function<String, T> parser,
    Function<DirectMessage, T> extractor
) implements CursorStrategy<T, DirectMessage> {

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

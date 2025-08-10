package com.stylemycloset.follow.repository.cursor.strategy;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.stylemycloset.follow.entity.Follow;
import org.springframework.data.domain.Sort.Direction;

public interface CursorStrategy<T extends Comparable<T>> {

  Path<T> path();

  T parse(String rawCursor);

  T extract(Follow instance);

  BooleanExpression buildPredicate(String rawDirection, String rawCursor);

  OrderSpecifier<T> buildOrder(String rawDirection, String rawCursor);

  default Direction parseDirectionOrDefault(String rawDirection) {
    return Direction.fromOptionalString(rawDirection)
        .orElse(Direction.DESC);
  }

}

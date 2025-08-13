package com.stylemycloset.common.repository.cursor;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.data.domain.Sort.Direction;

public interface CursorStrategy<T extends Comparable<T>, E> {

  Path<T> path();

  T parse(String rawCursor);

  T extract(E instance);

  BooleanExpression buildInequalityPredicate(String rawDirection, String rawCursor);

  OrderSpecifier<T> buildOrder(String rawDirection, String rawCursor);

  BooleanExpression buildEq(String rawCursor);

  default Direction parseDirectionOrDefault(String rawDirection) {
    return Direction.fromOptionalString(rawDirection)
        .orElse(Direction.DESC);
  }

}
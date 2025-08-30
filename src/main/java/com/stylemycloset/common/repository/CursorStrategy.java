package com.stylemycloset.common.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.data.domain.Sort.Direction;

public interface CursorStrategy<T extends Comparable<T>, E> {

  Path<T> path();

  T parse(String rawCursor);

  T extract(E instance);

  BooleanExpression buildInequalityPredicate(Direction direction, String rawCursor);

  BooleanExpression buildEq(String rawCursor);

  default boolean isDescendingOrDefault(Direction direction) {
    return direction == null || direction.isDescending();
  }

  default OrderSpecifier<T> buildOrder(Direction direction) {
    if (isDescendingOrDefault(direction)) {
      return new OrderSpecifier<>(Order.DESC, path());
    }
    return new OrderSpecifier<>(Order.ASC, path());
  }

  default BooleanExpression buildCursorPredicate(
      Direction direction,
      String rawCursor,
      String idAfter,
      CursorStrategy<?, E> idAfterStrategy
  ) {
    BooleanExpression cursorPredicate = buildInequalityPredicate(direction, rawCursor);
    BooleanExpression cursorEqualExpression = buildEq(rawCursor);
    BooleanExpression idAfterPredicate = idAfterStrategy.buildInequalityPredicate(direction,
        idAfter);
    if (cursorEqualExpression == null || idAfterPredicate == null) {
      return cursorPredicate;
    }
    return cursorPredicate.or(
        cursorEqualExpression.and(idAfterPredicate)
    );
  }

}
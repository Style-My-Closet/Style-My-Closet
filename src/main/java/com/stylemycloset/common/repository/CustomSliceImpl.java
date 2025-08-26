package com.stylemycloset.common.repository;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

public final class CustomSliceImpl<T> extends SliceImpl<T> {

  private CustomSliceImpl(List<T> content, Pageable pageable, boolean hasNext) {
    super(content, pageable, hasNext);
  }

  public static <T> CustomSliceImpl<T> of(
      List<T> contents,
      int limit,
      CursorStrategy<?, T> primaryCursorStrategy,
      Direction direction
  ) {
    Sort sort = Sort.by(
        getDirection(direction),
        primaryCursorStrategy.path().getMetadata().getName()
    );

    return new CustomSliceImpl<>(
        contents.subList(0, Math.min(contents.size(), limit)),
        PageRequest.of(0, limit, sort),
        contents.size() > limit
    );
  }

  private static Direction getDirection(Direction direction) {
    if (direction == null) {
      direction = Direction.DESC;
    }
    return direction;
  }

  public static <T> Order getOrder(Slice<T> attributeDefinitions) {
    return attributeDefinitions.getPageable()
        .getSort()
        .stream()
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("DTO 변환시 정렬 순서(Order)가 존재하지 않습니다."));
  }

}

package com.stylemycloset.follow.repository.cursor;

import com.stylemycloset.follow.entity.Follow;
import com.stylemycloset.follow.entity.QFollow;
import com.stylemycloset.follow.repository.cursor.strategy.impl.ChronologicalCursorStrategy;
import com.stylemycloset.follow.repository.cursor.strategy.CursorStrategy;
import com.stylemycloset.follow.repository.cursor.strategy.impl.NumberCursorStrategy;
import java.time.Instant;
import java.util.Arrays;

public enum FollowCursorField {

  ID(new NumberCursorStrategy<>(
      QFollow.follow.id,
      Long::parseLong,
      Follow::getId)
  ),

  CREATED_AT(new ChronologicalCursorStrategy(
      QFollow.follow.createdAt,
      Instant::parse,
      Follow::getCreatedAt)
  ),

  UPDATED_AT(new ChronologicalCursorStrategy(
      QFollow.follow.updatedAt,
      Instant::parse,
      Follow::getUpdatedAt)
  ),

  FOLLOWED_AT(new ChronologicalCursorStrategy(
      QFollow.follow.followedAt,
      Instant::parse,
      Follow::getFollowedAt)
  );

  private final CursorStrategy<?> cursorStrategy;

  FollowCursorField(CursorStrategy<?> cursorStrategy) {
    this.cursorStrategy = cursorStrategy;
  }

  public static CursorStrategy<?> resolveStrategy(String sortBy) {
    if (sortBy == null || sortBy.isBlank()) {
      return CREATED_AT.cursorStrategy;
    }

    return Arrays.stream(FollowCursorField.values())
        .filter(field -> isSameName(field, sortBy.trim()))
        .findFirst()
        .map(followCursorField -> followCursorField.cursorStrategy)
        .orElseThrow(() -> new IllegalArgumentException("요청하신 정렬 기준 필드명에 맞는 필드명이 없습니다."));
  }

  private static boolean isSameName(FollowCursorField field, String sortBy) {
    return field.cursorStrategy
        .path()
        .getMetadata()
        .getName()
        .equalsIgnoreCase(sortBy);
  }

}


package com.stylemycloset.directmessage.repository.cursor.strategy;

import com.stylemycloset.directmessage.entity.DirectMessage;
import com.stylemycloset.directmessage.entity.QDirectMessage;
import com.stylemycloset.directmessage.repository.cursor.CursorStrategy;
import com.stylemycloset.directmessage.repository.cursor.strategy.impl.ChronologicalCursorStrategy;
import com.stylemycloset.directmessage.repository.cursor.strategy.impl.NumberCursorStrategy;
import java.time.Instant;
import java.util.Arrays;

public enum DirectMessageField {

  ID(new NumberCursorStrategy<>(
      QDirectMessage.directMessage.id,
      Long::parseLong,
      DirectMessage::getId)
  ),

  CREATED_AT(new ChronologicalCursorStrategy(
      QDirectMessage.directMessage.createdAt,
      Instant::parse,
      DirectMessage::getCreatedAt)
  ),

  UPDATED_AT(new ChronologicalCursorStrategy(
      QDirectMessage.directMessage.updatedAt,
      Instant::parse,
      DirectMessage::getUpdatedAt)
  );

  private final CursorStrategy<?> cursorStrategy;

  DirectMessageField(CursorStrategy<?> cursorStrategy) {
    this.cursorStrategy = cursorStrategy;
  }

  public static CursorStrategy<?> resolveStrategy(String sortBy) {
    if (sortBy == null || sortBy.isBlank()) {
      return CREATED_AT.cursorStrategy;
    }

    return Arrays.stream(DirectMessageField.values())
        .filter(field -> isSameName(field, sortBy.trim()))
        .findFirst()
        .map(messageCursorField -> messageCursorField.cursorStrategy)
        .orElseThrow(() -> new IllegalArgumentException("요청하신 정렬 기준 필드명에 맞는 필드명이 없습니다."));
  }

  private static boolean isSameName(DirectMessageField field, String sortBy) {
    return field.cursorStrategy
        .path()
        .getMetadata()
        .getName()
        .equalsIgnoreCase(sortBy);
  }

}

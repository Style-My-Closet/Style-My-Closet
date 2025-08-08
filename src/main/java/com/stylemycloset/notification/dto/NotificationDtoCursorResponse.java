package com.stylemycloset.notification.dto;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import lombok.Builder;

@Builder
public record NotificationDtoCursorResponse(
    List<NotificationDto> data,
    Instant nextCursor,
    Long nextIdAfter,
    boolean hasNext,
    long totalCount,
    String sortBy,
    String sortDirection
) {
  public static NotificationDtoCursorResponse of(
      List<NotificationDto> data,
      Instant nextCursor,
      Long nextIdAfter,
      boolean hasNext,
      long totalCount
  ) {
    return NotificationDtoCursorResponse.builder()
      .data(Collections.unmodifiableList(data))
      .nextCursor(nextCursor)
        .nextIdAfter(nextIdAfter)
        .hasNext(hasNext)
        .totalCount(totalCount)
        .sortBy("createdAt")
        .sortDirection("DESCENDING")
        .build();
  }
}

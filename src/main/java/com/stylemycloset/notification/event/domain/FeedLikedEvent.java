package com.stylemycloset.notification.event.domain;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public record FeedLikedEvent(
    @NotNull Long feedId,
    @NotNull Long likeUserId
) {
  public FeedLikedEvent {
    Objects.requireNonNull(feedId, "feedId는 null일 수 없음");
    Objects.requireNonNull(likeUserId, "likeUserId는 null일 수 없음");
  }
}
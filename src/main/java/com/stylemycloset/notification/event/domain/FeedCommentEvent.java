package com.stylemycloset.notification.event.domain;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public record FeedCommentEvent(
    @NotNull Long feedId,
    @NotNull Long feedCommentAuthorId
) {
  public FeedCommentEvent {
    Objects.requireNonNull(feedId, "feedId는 null일 수 없음");
    Objects.requireNonNull(feedCommentAuthorId, "feedCommentAuthorId는 null일 수 없음");
  }
}

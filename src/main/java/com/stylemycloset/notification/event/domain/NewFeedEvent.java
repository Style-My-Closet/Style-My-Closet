package com.stylemycloset.notification.event.domain;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public record NewFeedEvent(
    @NotNull Long feedId,
    @NotNull String feedContent,
    @NotNull Long feedAuthorId
) {
  public NewFeedEvent {
    Objects.requireNonNull(feedId, "feedId는 null일 수 없음");
    Objects.requireNonNull(feedContent, "feedContent는 null일 수 없음");
    Objects.requireNonNull(feedAuthorId, "feedAuthorId는 null일 수 없음");
  }
}

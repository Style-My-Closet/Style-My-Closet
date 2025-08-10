package com.stylemycloset.notification.event.domain;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public record FollowEvent(
    @NotNull Long receiverId,
    @NotNull String followUsername
) {
  public FollowEvent {
    Objects.requireNonNull(receiverId, "receiverId는 null일 수 없음");
    Objects.requireNonNull(followUsername, "followUsername는 null일 수 없음");
  }
}

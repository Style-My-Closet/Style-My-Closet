package com.stylemycloset.notification.event.domain;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public record DMSentEvent(
    @NotNull Long messageId,
    @NotNull String sendUsername
) {
  public DMSentEvent {
    Objects.requireNonNull(messageId, "messageId는 null일 수 없음");
    Objects.requireNonNull(sendUsername, "sendUsername는 null일 수 없음");
  }
}

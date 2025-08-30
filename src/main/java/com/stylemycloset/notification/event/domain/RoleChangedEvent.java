package com.stylemycloset.notification.event.domain;

import com.stylemycloset.user.entity.Role;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public record RoleChangedEvent (
    @NotNull Long receiverId,
    @NotNull Role previousRole
) {
  public RoleChangedEvent {
    Objects.requireNonNull(receiverId, "receiverId는 null일 수 없음");
    Objects.requireNonNull(previousRole, "previousRole는 null일 수 없음");
  }
}

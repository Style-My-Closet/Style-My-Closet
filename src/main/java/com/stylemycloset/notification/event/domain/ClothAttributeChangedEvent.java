package com.stylemycloset.notification.event.domain;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public record ClothAttributeChangedEvent(
    @NotNull Long clothAttributeId,
    @NotNull String changedAttributeName
) {
  public ClothAttributeChangedEvent {
    Objects.requireNonNull(clothAttributeId, "clothAttributeId는 null일 수 없음");
    Objects.requireNonNull(changedAttributeName, "changedAttributeName는 null일 수 없음");
  }
}

package com.stylemycloset.notification.event.domain;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public record NewClothAttributeEvent(
    @NotNull Long clothAttributeId,
    @NotNull String attributeName
) {
  public NewClothAttributeEvent {
    Objects.requireNonNull(clothAttributeId, "clothAttributeId는 null일 수 없음");
    Objects.requireNonNull(attributeName, "attributeName는 null일 수 없음");
  }
}

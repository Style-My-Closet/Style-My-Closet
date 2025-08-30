package com.stylemycloset.notification.event.domain;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public record WeatherAlertEvent(
    @NotNull Long receiverId,
    @NotNull Long weatherId,
    @NotNull String message
) {
  public WeatherAlertEvent {
    Objects.requireNonNull(receiverId, "receiverId는 null일 수 없음");
    Objects.requireNonNull(weatherId, "weatherId는 null일 수 없음");
    Objects.requireNonNull(message, "message는 null일 수 없음");
  }
}

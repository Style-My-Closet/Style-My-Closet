package com.stylemycloset.sse.dto;

import com.stylemycloset.notification.dto.NotificationDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationDtoWithId(
    @NotBlank String eventId,
    @NotNull NotificationDto dto
) {
}

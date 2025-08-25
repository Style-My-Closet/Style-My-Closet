package com.stylemycloset.sse.dto;

import com.stylemycloset.notification.dto.NotificationDto;

public record NotificationDtoWithId(
    String eventId,
    NotificationDto dto
) {
}

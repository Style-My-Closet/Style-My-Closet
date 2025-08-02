package com.stylemycloset.notification.dto;

import jakarta.validation.constraints.Positive;
import java.time.Instant;

public record NotificationFindAllRequest(
    Instant cursor,
    Long idAfter,
    @Positive int limit
) {

}

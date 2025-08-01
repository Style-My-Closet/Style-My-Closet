package com.stylemycloset.notification.dto;

import java.time.Instant;

public record NotificationFindAllRequest(
    Instant cursor,
    Long idAfter,
    int limit
) {

}

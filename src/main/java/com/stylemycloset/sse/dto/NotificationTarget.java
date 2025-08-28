package com.stylemycloset.sse.dto;

import java.util.Set;

public record NotificationTarget(
    Set<Long> userIds
) {

}

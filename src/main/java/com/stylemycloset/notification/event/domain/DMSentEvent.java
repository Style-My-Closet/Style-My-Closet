package com.stylemycloset.notification.event.domain;

public record DMSentEvent(
    Long messageId,
    String sendUsername
) {

}

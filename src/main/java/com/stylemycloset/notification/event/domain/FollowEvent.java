package com.stylemycloset.notification.event.domain;

public record FollowEvent(
    Long receiverId,
    String followUsername
) {

}

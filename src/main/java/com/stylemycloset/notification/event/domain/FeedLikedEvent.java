package com.stylemycloset.notification.event.domain;

public record FeedLikedEvent(
    Long feedId,
    String feedContent,
    Long receiverId,
    String likedByUsername
) {

}

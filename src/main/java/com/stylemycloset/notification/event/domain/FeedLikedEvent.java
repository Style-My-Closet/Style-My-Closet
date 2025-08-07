package com.stylemycloset.notification.event.domain;

public record FeedLikedEvent(
    Long feedId,
    Long likeUserId
) {

}
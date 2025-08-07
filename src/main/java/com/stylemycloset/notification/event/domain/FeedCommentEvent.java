package com.stylemycloset.notification.event.domain;

public record FeedCommentEvent(
    Long feedId,
    String commentAuthorUsername,
    String commentContent,
    Long receiverId
) {

}

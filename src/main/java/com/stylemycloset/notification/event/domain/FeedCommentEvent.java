package com.stylemycloset.notification.event.domain;

public record FeedCommentEvent(
    Long feedId,
    Long feedCommentAuthorId
) {

}

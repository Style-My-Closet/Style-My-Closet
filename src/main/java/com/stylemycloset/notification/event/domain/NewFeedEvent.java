package com.stylemycloset.notification.event.domain;

public record NewFeedEvent(
    Long feedId,
    String feedContent,
    Long feedAuthorId
) {

}

package com.stylemycloset.notification.event;

import com.stylemycloset.user.entity.User;

public record FeedCommentEvent(
    Long feedId,
    String commentAuthorUsername,
    String commentContent,
    User feedAuthor
) {

}

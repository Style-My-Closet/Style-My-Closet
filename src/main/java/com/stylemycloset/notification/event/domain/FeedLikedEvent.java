package com.stylemycloset.notification.event.domain;

import com.stylemycloset.user.entity.User;

public record FeedLikedEvent(
    Long feedId,
    String feedContent,
    User receiver,
    String likedByUsername
) {

}

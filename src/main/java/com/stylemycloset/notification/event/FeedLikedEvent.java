package com.stylemycloset.notification.event;

import com.stylemycloset.user.entity.User;

public record FeedLikedEvent(
    Long feedId,
    String feedContent,
    User receiver,
    String likedByUsername
) {

}

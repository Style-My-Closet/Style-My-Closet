package com.stylemycloset.notification.event.domain;

import com.stylemycloset.user.entity.Role;

public record RoleChangedEvent (
    Long receiverId,
    Role changedRole
) {

}

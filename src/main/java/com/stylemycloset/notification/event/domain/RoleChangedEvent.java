package com.stylemycloset.notification.event.domain;

import com.stylemycloset.user.entity.Role;
import com.stylemycloset.user.entity.User;

public record RoleChangedEvent (
    User user,
    Role changedRole
) {

}

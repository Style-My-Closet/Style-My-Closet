package com.stylemycloset.user.dto.request;

import com.stylemycloset.user.entity.Role;

public record UserRoleUpdateRequest(
        Role role
) {
}

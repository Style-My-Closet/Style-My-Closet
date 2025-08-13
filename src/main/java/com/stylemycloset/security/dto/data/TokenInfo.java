package com.stylemycloset.security.dto.data;

import com.stylemycloset.user.entity.Role;

public record TokenInfo(
    Long userId,
    String role,
    String name
) {

}

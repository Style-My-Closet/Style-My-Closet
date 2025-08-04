package com.stylemycloset.user.dto.data;

import com.querydsl.core.annotations.QueryProjection;
import com.stylemycloset.user.entity.Role;

import java.time.Instant;
import java.util.List;

public record UserDto(
    Long id,
    Instant createdAt,
    String email,
    String name,
    Role role,
    List<String> linkedOAuthProviders,
    boolean locked
) {

}

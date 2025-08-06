package com.stylemycloset.user.dto.request;

public record UserCreateRequest(
    String name,
    String email,
    String password
) {
}

package com.stylemycloset.user.dto;

public record UserCreateRequest(
    String name,
    String email,
    String password
) {
}
package com.stylemycloset.security.dto.request;

public record SignInRequest(
    String email,
    String password
) {

}

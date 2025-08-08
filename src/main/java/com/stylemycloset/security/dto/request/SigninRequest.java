package com.stylemycloset.security.dto.request;

public record SigninRequest(
    String email,
    String password
) {

}

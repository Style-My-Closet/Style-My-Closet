package com.stylemycloset.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.util.UriComponentsBuilder;

public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException exception) throws IOException, ServletException {

    String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:8080/login")
        .queryParam("error", true)
        .queryParam("message", "카카오 로그인에 실패했습니다.")
        .build().toUriString();

    response.sendRedirect(targetUrl);
  }
}

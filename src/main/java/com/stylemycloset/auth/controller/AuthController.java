package com.stylemycloset.auth.controller;

import com.stylemycloset.security.jwt.JwtService;
import com.stylemycloset.security.jwt.JwtSession;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final JwtService jwtService;

  @GetMapping("/csrf-token")
  public ResponseEntity<java.util.Map<String, String>> getCsrfToken(CsrfToken csrfToken) {
    log.debug("Csrf 토큰 요청");
    if (csrfToken == null) {
      return ResponseEntity.status(HttpStatus.OK).body(java.util.Map.of());
    }
    java.util.Map<String, String> body = new java.util.HashMap<>();
    body.put("headerName", csrfToken.getHeaderName());
    body.put("token", csrfToken.getToken());
    return ResponseEntity.status(HttpStatus.OK).body(body);
  }

  @GetMapping("/me")
  public ResponseEntity<String> me(
      @CookieValue(value = JwtService.REFRESH_TOKEN_COOKIE_NAME) String refreshToken) {
    log.info("내 정보 조회 요청");
    JwtSession jwtSession = jwtService.getJwtSession(refreshToken);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(jwtSession.getAccessToken());
  }

  @PostMapping("/refresh")
  public ResponseEntity<String> refresh(
      @CookieValue(JwtService.REFRESH_TOKEN_COOKIE_NAME) String refreshToken,
      HttpServletResponse response
  ) {
    log.info("토큰 재발급 요청");
    JwtSession jwtSession = jwtService.refreshJwtSession(refreshToken);

    Cookie refreshTokenCookie = new Cookie(JwtService.REFRESH_TOKEN_COOKIE_NAME,
        jwtSession.getRefreshToken());
    refreshTokenCookie.setHttpOnly(true);
    response.addCookie(refreshTokenCookie);

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(jwtSession.getAccessToken())
        ;
  }


}

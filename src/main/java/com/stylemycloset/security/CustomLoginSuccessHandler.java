package com.stylemycloset.security;

import com.stylemycloset.security.jwt.JwtService;
import com.stylemycloset.security.jwt.JwtSession;
import com.stylemycloset.user.dto.data.UserDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@RequiredArgsConstructor
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

  private final JwtService jwtService;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    ClosetUserDetails principal = (ClosetUserDetails) authentication.getPrincipal();
    UserDto userDto = new UserDto(
        principal.getUserId(),
        null,
        principal.getEmail(),
        principal.getUsername(),
        principal.getRole(),
        null,
        false
    );

    JwtSession jwtSession = jwtService.createJwtSession(userDto);
    String accessToken = jwtSession.getAccessToken();
    String refreshToken = jwtSession.getRefreshToken();

    Cookie refreshTokenCookie = new Cookie(JwtService.REFRESH_TOKEN_COOKIE_NAME, refreshToken);
    refreshTokenCookie.setHttpOnly(true);
    response.addCookie(refreshTokenCookie);

    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    response.getWriter().write(accessToken);
  }
}

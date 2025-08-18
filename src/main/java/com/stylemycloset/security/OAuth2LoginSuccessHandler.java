package com.stylemycloset.security;

import com.stylemycloset.security.jwt.JwtService;
import com.stylemycloset.security.jwt.JwtSession;
import com.stylemycloset.user.dto.data.UserDto;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.mapper.UserMapper;
import com.stylemycloset.user.repository.UserRepository;
import com.stylemycloset.user.exception.UserNotFoundException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

  private final JwtService jwtService;
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    String email = (String) oAuth2User.getAttributes().get("email");

    User user = userRepository.findByEmail(email)
        .orElseThrow(UserNotFoundException::new);

    UserDto userDto = userMapper.UsertoUserDto(user);

    JwtSession jwtSession = jwtService.createJwtSession(userDto);
    String refreshToken = jwtSession.getRefreshToken();

    Cookie refreshTokenCookie = new Cookie(JwtService.REFRESH_TOKEN_COOKIE_NAME, refreshToken);
    refreshTokenCookie.setHttpOnly(true);
    refreshTokenCookie.setPath("/");
    response.addCookie(refreshTokenCookie);
    
    String targetUrl = UriComponentsBuilder.fromUriString(
            "http://localhost:8080/")
        .build().toUriString();
    response.sendRedirect(targetUrl);

  }

}

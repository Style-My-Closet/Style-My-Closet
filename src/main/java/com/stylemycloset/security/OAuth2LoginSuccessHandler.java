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
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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
    Map<String, Object> attributes = oAuth2User.getAttributes();

    String email;

    OAuth2AuthenticationToken authenticationToken = (OAuth2AuthenticationToken) authentication;

    String registrationId = authenticationToken.getAuthorizedClientRegistrationId();

    if ("kakao".equals(registrationId)) {
      Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
      Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
      String nickname = (String) profile.get("nickname");
      email = nickname + "@kakao.com";
    } else {
      email = (String) attributes.get("email");
    }

    User user = userRepository.findByEmail(email)
        .orElseThrow(UserNotFoundException::new);

    UserDto userDto = userMapper.toUserDto(user);

    JwtSession jwtSession = jwtService.createJwtSession(userDto);
    String refreshToken = jwtSession.getRefreshToken();

    Cookie refreshTokenCookie = new Cookie(JwtService.REFRESH_TOKEN_COOKIE_NAME, refreshToken);
    refreshTokenCookie.setHttpOnly(true);
    refreshTokenCookie.setPath("/");
    response.addCookie(refreshTokenCookie);

    String targetUrl = UriComponentsBuilder.fromUriString(
            "/")
        .build().toUriString();
    response.sendRedirect(targetUrl);

  }

}

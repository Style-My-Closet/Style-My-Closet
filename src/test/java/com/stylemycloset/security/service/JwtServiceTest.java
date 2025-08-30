package com.stylemycloset.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.stylemycloset.security.jwt.JwtBlacklist;
import com.stylemycloset.security.jwt.JwtService;
import com.stylemycloset.security.jwt.JwtSession;
import com.stylemycloset.security.jwt.JwtSessionRepository;
import com.stylemycloset.user.dto.data.UserDto;
import com.stylemycloset.user.entity.Role;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

  @InjectMocks
  private JwtService jwtService;

  @Mock
  private JwtSessionRepository jwtSessionRepository;

  @Mock
  private JwtBlacklist jwtBlacklist;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(jwtService, "secret",
        "this-is-a-super-secret-key-for-unit-testing-1234567890");
    ReflectionTestUtils.setField(jwtService, "accessTokenValiditySeconds", 3600L);
    ReflectionTestUtils.setField(jwtService, "refreshTokenValiditySeconds", 86400L);
  }

  @Test
  @DisplayName("JWT세션 생성 테스트")
  void createJwtSessionTest() {
    //given
    UserDto testUserDto = new UserDto(1L, Instant.now(), "test@naver.com", "tester", Role.USER,
        null, false);

    given(jwtSessionRepository.findByUserId(testUserDto.id())).willReturn(Optional.empty());
    given(jwtSessionRepository.save(any(JwtSession.class))).willAnswer(
        invocation -> invocation.getArgument(0));

    //when
    JwtSession jwtSession = jwtService.createJwtSession(testUserDto);

    //then
    assertNotNull(jwtSession);
    assertEquals(jwtSession.getUserId(), testUserDto.id());
    assertNotNull(jwtSession.getAccessToken());
    assertNotNull(jwtSession.getRefreshToken());

  }

  @Test
  @DisplayName("RefreshToken으로 JWT세션 받아오는 테스트")
  void refreshJwtSessionTest() {
    //given
    String refreshToken = "refresh-token";
    JwtSession jwtSession = new JwtSession(1L, "access-token", refreshToken, Instant.now());

    given(jwtSessionRepository.findByRefreshToken(refreshToken)).willReturn(
        Optional.of(jwtSession));

    //when
    JwtSession result = jwtService.getJwtSession(refreshToken);

    //then
    assertNotNull(result);
    assertEquals(jwtSession.getUserId(), result.getUserId());
    assertEquals(jwtSession.getAccessToken(), result.getAccessToken());
    assertEquals(jwtSession.getRefreshToken(), result.getRefreshToken());
  }

  @Test
  @DisplayName("UserId로 JWT 세션 무효화")
  void invalid_userId_jwtSessionTest() {
    //given
    JwtSession jwtSession = new JwtSession(1L, "access-token", "refresh-token", Instant.now());
    given(jwtSessionRepository.findByUserId(jwtSession.getUserId())).willReturn(
        Optional.of(jwtSession));

    //when
    jwtService.invalidateJwtSession(jwtSession.getUserId());

    //then
    verify(jwtSessionRepository, times(1)).delete(jwtSession);

  }

  @Test
  @DisplayName("Refresh-Token으로 JWT 세션 무효화")
  void invalid_refresh_jwtSessionTest() {
    //given
    JwtSession jwtSession = new JwtSession(1L, "access-token", "refresh-token", Instant.now());
    given(jwtSessionRepository.findByRefreshToken(jwtSession.getRefreshToken())).willReturn(
        Optional.of(jwtSession));

    //when
    jwtService.invalidateJwtSession(jwtSession.getRefreshToken());

    //then
    verify(jwtSessionRepository, times(1)).delete(jwtSession);

  }

  @Test
  @DisplayName("토큰 유효성 검사")
  void valid_tokenTest() {
    //given
    UserDto userDto = new UserDto(1L, Instant.now(), "test@naver.com", "tester", Role.USER, null,
        false);
    String accessToken = jwtService.createJwtSession(userDto).getAccessToken();

    //when
    boolean isValid = jwtService.validate(accessToken);

    //then
    assertTrue(isValid);
  }

}

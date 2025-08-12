package com.stylemycloset.security.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stylemycloset.security.dto.request.SigninRequest;
import com.stylemycloset.user.entity.Role;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityControllerTest {

  @Autowired
  private TestRestTemplate restTemplate; // 실제 HTTP 요청을 보내는 클라이언트

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private ObjectMapper objectMapper;

  private static final String TEST_EMAIL = "testuser@example.com";
  private static final String TEST_PASSWORD = "password123";

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
    User testUser = new User("tester", TEST_EMAIL, passwordEncoder.encode(TEST_PASSWORD));
    userRepository.saveAndFlush(testUser);
  }


  @Test
  @DisplayName("로그인 성공")
  void login_Success() throws Exception {
    //given
    ResponseEntity<String> csrfResponse = restTemplate.getForEntity("/api/auth/csrf-token",
        String.class);
    String csrfResponseBody = csrfResponse.getBody();
    String csrfCookieHeader = csrfResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    Map<String, String> csrfTokenMap = objectMapper.readValue(csrfResponseBody, Map.class);

    SigninRequest signinRequest = new SigninRequest(TEST_EMAIL, TEST_PASSWORD);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add("Cookie", csrfCookieHeader);
    headers.add(csrfTokenMap.get("headerName"), csrfTokenMap.get("token"));
    HttpEntity<SigninRequest> loginRequestEntity = new HttpEntity<>(signinRequest, headers);

    //when
    ResponseEntity<String> loginResponse = restTemplate.postForEntity("/api/auth/sign-in",
        loginRequestEntity, String.class);

    //then
    assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(loginResponse.getBody()).isNotNull().startsWith("ey");
    assertThat(loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE)).contains(
        "refresh_token");
  }

  @Test
  @DisplayName("내 정보 조회 성공")
  void me_Success() throws Exception {
    ResponseEntity<String> loginResponse = performLogin();
    String originalAccessToken = loginResponse.getBody();
    String refreshTokenCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

    //given
    HttpHeaders headers = new HttpHeaders();
    headers.add("Cookie", refreshTokenCookie);
    HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

    //when
    ResponseEntity<String> meResponse = restTemplate.exchange("/api/auth/me", HttpMethod.GET,
        requestEntity, String.class);

    //then
    assertThat(meResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(meResponse.getBody()).isEqualTo(originalAccessToken);
  }

  @Test
  @DisplayName("토큰 재발급 성공")
  void refreshToken_Success() throws Exception {
    //given
    ResponseEntity<String> loginResponse = performLogin();
    String refreshTokenCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

    ResponseEntity<String> csrfResponse = restTemplate.getForEntity("/api/auth/csrf-token",
        String.class);
    String csrfResponseBody = csrfResponse.getBody();
    String csrfCookieHeader = csrfResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    Map<String, String> csrfTokenMap = objectMapper.readValue(csrfResponseBody, Map.class);

    HttpHeaders headers = new HttpHeaders();
    headers.add("Cookie", refreshTokenCookie);
    headers.add("Cookie", csrfCookieHeader);
    headers.add(csrfTokenMap.get("headerName"), csrfTokenMap.get("token"));

    HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

    //when
    ResponseEntity<String> refreshResponse = restTemplate.postForEntity("/api/auth/refresh",
        requestEntity, String.class);

    //then
    assertThat(refreshResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(refreshResponse.getBody()).isNotNull();
  }

  @Test
  @DisplayName("로그아웃 성공")
  void signOut_Success() throws Exception {
    //given
    ResponseEntity<String> loginResponse = performLogin();
    String refreshTokenCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

    HttpHeaders headers = new HttpHeaders();
    headers.add("Cookie", refreshTokenCookie);
    HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

    //when
    ResponseEntity<Void> logoutResponse = restTemplate.postForEntity("/api/auth/sign-out",
        requestEntity, Void.class);

    //then
    assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(logoutResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE)).contains("Max-Age=0");
  }

  //헬퍼 메소드
  private ResponseEntity<String> performLogin() throws Exception {
    ResponseEntity<String> csrfResponse = restTemplate.getForEntity("/api/auth/csrf-token",
        String.class);
    String csrfResponseBody = csrfResponse.getBody();
    String csrfCookieHeader = csrfResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    Map<String, String> csrfTokenMap = objectMapper.readValue(csrfResponseBody, Map.class);

    SigninRequest signinRequest = new SigninRequest(TEST_EMAIL, TEST_PASSWORD);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add("Cookie", csrfCookieHeader);
    headers.add(csrfTokenMap.get("headerName"), csrfTokenMap.get("token"));
    HttpEntity<SigninRequest> loginRequestEntity = new HttpEntity<>(signinRequest, headers);

    return restTemplate.postForEntity("/api/auth/sign-in", loginRequestEntity, String.class);
  }
}
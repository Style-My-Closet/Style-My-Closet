package com.stylemycloset.sse.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.stylemycloset.IntegrationTestSupport;
import com.stylemycloset.notification.dto.NotificationDto;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.security.jwt.JwtService;
import com.stylemycloset.security.jwt.JwtSession;
import com.stylemycloset.sse.cache.SseNotificationInfoCache;
import com.stylemycloset.user.dto.data.UserDto;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.mapper.UserMapper;
import com.stylemycloset.user.repository.UserRepository;
import java.io.BufferedReader;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.jline.utils.InputStreamReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SseControllerTest extends IntegrationTestSupport {

  @LocalServerPort
  private int port;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  JwtService jwtService;
  @Autowired
  UserMapper userMapper;
  @Autowired
  SseNotificationInfoCache cache;
  @Autowired
  RedisConnectionFactory connectionFactory;

  @AfterEach
  void tearDown() {
    userRepository.deleteAllInBatch();
    try (var connection = connectionFactory.getConnection()) {
      connection.serverCommands().flushAll();
    }
  }

  User user;

  String createUser() {
    user = userRepository.save(new User("test", "test@test.com", "test"));
    UserDto userDto = userMapper.toUserDto(user);
    JwtSession session = jwtService.createJwtSession(userDto);
    return session.getAccessToken();
  }

  @DisplayName("lastEventId 없이 SSE 연결 요청을 보내면 SSE 연결 응답을 반환한다")
  @Test
  public void sseConnect_returnSseEmitter() throws Exception {
    String accessToken = createUser();

    HttpClient client = HttpClient.newHttpClient();
    URI url = URI.create("http://localhost:" + port + "/api/sse");

    HttpRequest req = HttpRequest.newBuilder(url)
        .header("Accept", "text/event-stream")
        .header("Authorization", "Bearer " + accessToken)
        .GET()
        .build();

    HttpResponse<InputStream> resp = client.send(req, HttpResponse.BodyHandlers.ofInputStream());

    assertEquals(200, resp.statusCode());
    assertTrue(resp.headers().firstValue("content-type").orElse("")
        .contains("text/event-stream"));

    resp.body().close();
  }

  @DisplayName("lastEventId를 포함하여 SSE 연결 요청을 보내면 놓친 알림 데이터를 전송하고, SSE 연결 응답을 반환한다")
  @Test
  public void sseReconnect_shouldReplayMissedEvents() throws Exception {
    String accessToken = createUser();
    Long userId = user.getId();

    NotificationDto notificationDto = new NotificationDto(10L, Instant.now(), userId, "데이터A", "데이터A", NotificationLevel.INFO);
    NotificationDto notificationDto2 = new NotificationDto(11L, Instant.now(), userId, "데이터B", "데이터B", NotificationLevel.INFO);
    List<NotificationDto> dtoList = List.of(notificationDto, notificationDto2);
    for(NotificationDto i : dtoList) {
      cache.addNotificationInfo(userId, i);
    }

    HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();
    URI url = URI.create("http://localhost:" + port + "/api/sse?LastEventId=1754037511000-0");

    HttpRequest req = HttpRequest.newBuilder(url)
        .header("Accept", "text/event-stream")
        .header("Authorization", "Bearer " + accessToken)
        .GET()
        .build();

    HttpResponse<InputStream> resp = client.send(req, HttpResponse.BodyHandlers.ofInputStream());

    assertEquals(200, resp.statusCode());
    assertTrue(resp.headers().firstValue("content-type").orElse("")
        .contains("text/event-stream"));

    boolean dataA = false, dataB = false;
    long deadline = System.currentTimeMillis() + 5000;

    try (
        InputStream in = resp.body();
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
    ) {
      String line; // SSE 응답 스트림에서 한 줄씩 꺼낸 문자열 가짐.
      while (System.currentTimeMillis() < deadline && (line = br.readLine()) != null) {
        if (line.contains("데이터A")) dataA = true;
        if (line.contains("데이터B")) dataB = true;
        if (dataA && dataB) break;
      }
    }

    assertThat(dataA).isTrue();
    assertThat(dataB).isTrue();
  }
}

package com.stylemycloset.notification.event.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import com.stylemycloset.IntegrationTestSupport;
import com.stylemycloset.notification.event.domain.ClothAttributeChangedEvent;
import com.stylemycloset.notification.util.TestUserFactory;
import com.stylemycloset.sse.service.impl.SseServiceImpl;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@ExtendWith(OutputCaptureExtension.class)
public class ClothAttributeChangedNotificationEventListenerIntegrationTest extends
    IntegrationTestSupport {

  @Autowired
  ClothAttributeChangedNotificationEventListener listener;
  @Autowired
  UserRepository userRepository;
  @Autowired
  SseServiceImpl sseService;
  @Autowired
  RedisConnectionFactory connectionFactory;
  @Autowired
  StringRedisTemplate template;

  String NOTIFICATION_KEY = "notification:";

  @BeforeEach
  void beforeEach() {
    clearAll();
  }

  @AfterEach
  void afterEach() {
    clearAll();
  }

  void clearAll() {
    userRepository.deleteAllInBatch();
    try (var connection = connectionFactory.getConnection()) {
      connection.serverCommands().flushAll();
    }
  }

  @DisplayName("의상 속성 변경 이벤트가 호출되면 알림을 생성하고 캐시에 저장 후, SSE 전송을 한다.")
  @Test
  void handleClothAttributeChangedEvent_sendSseMessage(CapturedOutput output) {
    // given
    User user1 = TestUserFactory.createUser(userRepository, "user1", "user1@test.test");
    Long user1Id = user1.getId();
    User user2 = TestUserFactory.createUser(userRepository, "user2", "user2@test.test");
    Long user2Id = user2.getId();

    String now = String.valueOf(System.currentTimeMillis());
    sseService.connect(user1Id, now + "-0", null);
    sseService.connect(user2.getId(), now + "-1", null);

    ClothAttributeChangedEvent event = new ClothAttributeChangedEvent(1L, "속성 변경");

    //when
    listener.handler(event);

    // then
    var user1Records = template.opsForStream().range(NOTIFICATION_KEY + user1Id, Range.unbounded());
    assertThat(user1Records).hasSize(1);
    String user1EventId = user1Records.getFirst().getId().getValue();

    var user2Records = template.opsForStream().range(NOTIFICATION_KEY + user2Id, Range.unbounded());
    assertThat(user2Records).hasSize(1);
    String user2EventId = user2Records.getFirst().getId().getValue();

    await().untilAsserted(() -> {
      String logs = output.getOut();
      assertThat(logs).contains(String.format("[%d] %s SSE 이벤트 전송 성공 (eventId: %s)",
          user1Id, "notifications", user1EventId));
      assertThat(logs).contains(String.format("[%d] %s SSE 이벤트 전송 성공 (eventId: %s)",
          user2Id, "notifications", user2EventId));
    });
  }


}

package com.stylemycloset.notification.event.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.stylemycloset.IntegrationTestSupport;
import com.stylemycloset.notification.event.domain.RoleChangedEvent;
import com.stylemycloset.notification.util.TestUserFactory;
import com.stylemycloset.sse.service.impl.SseServiceImpl;
import com.stylemycloset.user.entity.Role;
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
public class RoleChangedNotificationEventListenerIntegrationTest extends IntegrationTestSupport {

  @Autowired
  RoleChangedNotificationEventListener listener;
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
      connection.serverCommands().flushDb();
    }
  }

  @DisplayName("권한 변경 이벤트가 호출되면 알림을 생성하고 캐시에 저장 후, SSE 전송을 한다.")
  @Test
  void handler_createsAndSendsNotification(CapturedOutput output) {
    // given
    User user = TestUserFactory.createUser(userRepository, "name", "test@test.email");
    Long userId = user.getId();

    String now = String.valueOf(System.currentTimeMillis());
    sseService.connect(userId, now, null);

    RoleChangedEvent roleChangedEvent = new RoleChangedEvent(user.getId(), Role.ADMIN);

    // when
    listener.handler(roleChangedEvent);

    // then
    var user1Records = template.opsForStream().range(NOTIFICATION_KEY + userId, Range.unbounded());
    assertThat(user1Records).hasSize(1);
    String user1EventId = user1Records.getFirst().getId().getValue();

    await().untilAsserted(() -> {
      String logs = output.getOut();
      assertThat(logs).contains(String.format("[%d] %s SSE 이벤트 전송 성공 (eventId: %s)",
          userId, "notifications", user1EventId));
    });
  }
}

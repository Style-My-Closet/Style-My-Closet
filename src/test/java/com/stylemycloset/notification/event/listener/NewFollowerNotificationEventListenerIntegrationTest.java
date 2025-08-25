package com.stylemycloset.notification.event.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.stylemycloset.IntegrationTestSupport;
import com.stylemycloset.follow.entity.Follow;
import com.stylemycloset.follow.repository.FollowRepository;
import com.stylemycloset.notification.event.domain.FollowEvent;
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
public class NewFollowerNotificationEventListenerIntegrationTest extends IntegrationTestSupport {

  @Autowired
  NewFollowerNotificationEventListener listener;
  @Autowired
  UserRepository userRepository;
  @Autowired
  FollowRepository followRepository;
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
    followRepository.deleteAllInBatch();
    try (var connection = connectionFactory.getConnection()) {
      connection.serverCommands().flushDb();
    }
  }

  @DisplayName("새로운 팔로우가 발생하면 알림을 생성하고 캐시에 저장 후, SSE 전송을 한다.")
  @Test
  void handleNewFollowerNotificationEvent_sendSseMessage(CapturedOutput output) {
    // given
    User follower = TestUserFactory.createUser(userRepository, "follower", "follower@test.com");
    User followee = TestUserFactory.createUser(userRepository, "followeeUser", "followeeUser@test.com");
    Long followeeId = followee.getId();

    Follow follow = new Follow(followee, follower);
    followRepository.save(follow);

    String now = String.valueOf(System.currentTimeMillis());
    sseService.connect(followee.getId(), now + "-0", null);

    FollowEvent followEvent = new FollowEvent(followee.getId(), follower.getName());

    // when
    listener.handler(followEvent);

    // then
    var user1Records = template.opsForStream().range(NOTIFICATION_KEY + followeeId, Range.unbounded());
    assertThat(user1Records).hasSize(1);
    String user1EventId = user1Records.getFirst().getId().getValue();

    await().untilAsserted(() -> {
      String logs = output.getOut();
      assertThat(logs).contains(String.format("[%d] %s SSE 이벤트 전송 성공 (eventId: %s)",
          followeeId, "notifications", user1EventId));
    });

  }
}

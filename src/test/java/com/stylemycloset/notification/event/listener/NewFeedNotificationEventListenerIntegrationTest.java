package com.stylemycloset.notification.event.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import com.stylemycloset.IntegrationTestSupport;
import com.stylemycloset.follow.entity.Follow;
import com.stylemycloset.follow.repository.FollowRepository;
import com.stylemycloset.notification.event.domain.NewFeedEvent;
import com.stylemycloset.notification.util.TestUserFactory;
import com.stylemycloset.ootd.entity.Feed;
import com.stylemycloset.ootd.repo.FeedRepository;
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
public class NewFeedNotificationEventListenerIntegrationTest extends IntegrationTestSupport {

  @Autowired
  NewFeedNotificationEventListener listener;
  @Autowired
  UserRepository userRepository;
  @Autowired
  FollowRepository followRepository;
  @Autowired
  FeedRepository feedRepository;
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
    feedRepository.deleteAllInBatch();
    try (var connection = connectionFactory.getConnection()) {
      connection.serverCommands().flushAll();
    }
  }

  @DisplayName("새로운 피드 생성 추가 이벤트가 호출되면 알림을 생성하고 캐시에 저장 후, SSE 전송을 한다.")
  @Test
  void handleNewFeedNotificationEvent_sendSseMessage(CapturedOutput output) {
    // given
    User feedAuthor = TestUserFactory.createUser(userRepository, "feedAuthor", "feedAuthor@test.test");
    User follower1 = TestUserFactory.createUser(userRepository, "follower1", "follower1@test.test");
    Long follower1Id = follower1.getId();
    User follower2 = TestUserFactory.createUser(userRepository, "follower2", "follower2@test.test");
    Long follower2Id = follower2.getId();

    Follow follow1 = new Follow(feedAuthor, follower1);
    followRepository.save(follow1);
    Follow follow2 = new Follow(feedAuthor, follower2);
    followRepository.save(follow2);

    Feed feed = Feed.createFeed(feedAuthor, null, "피드 내용");
    feedRepository.save(feed);

    String now = String.valueOf(System.currentTimeMillis());
    sseService.connect(follower1Id, now + "-0", null);
    sseService.connect(follower2Id, now + "-1", null);

    NewFeedEvent event = new NewFeedEvent(1L, "피드 테스트", feedAuthor.getId());

    // when
    listener.handler(event);

    // then
    var user1Records = template.opsForStream().range(NOTIFICATION_KEY + follower1Id, Range.unbounded());
    assertThat(user1Records).hasSize(1);
    String user1EventId = user1Records.getFirst().getId().getValue();

    var user2Records = template.opsForStream().range(NOTIFICATION_KEY + follower2Id, Range.unbounded());
    assertThat(user2Records).hasSize(1);
    String user2EventId = user2Records.getFirst().getId().getValue();

    await().untilAsserted(() -> {
      String logs = output.getOut();
      assertThat(logs).contains(String.format("[%d] %s SSE 이벤트 전송 성공 (eventId: %s)",
          follower1Id, "notifications", user1EventId));
      assertThat(logs).contains(String.format("[%d] %s SSE 이벤트 전송 성공 (eventId: %s)",
          follower2Id, "notifications", user2EventId));
    });
  }
}

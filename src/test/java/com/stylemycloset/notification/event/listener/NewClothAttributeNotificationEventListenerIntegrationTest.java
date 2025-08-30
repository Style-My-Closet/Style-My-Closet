package com.stylemycloset.notification.event.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.stylemycloset.IntegrationTestSupport;
import com.stylemycloset.notification.event.domain.NewClothAttributeEvent;
import com.stylemycloset.notification.util.TestUserFactory;
import com.stylemycloset.sse.service.impl.SseServiceImpl;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
public class NewClothAttributeNotificationEventListenerIntegrationTest extends IntegrationTestSupport {

  @Autowired
  NewClothAttributeNotificationEventListener listener;
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
  @DisplayName("의상 속성 추가 이벤트가 호출되면 알림을 생성하고 SSE로 전송 후 로그를 띄운다")
  @Test
  void handleNewClothAttributeEvent_sendSseMessage(CapturedOutput output) {
    // given
    User user1 = TestUserFactory.createUser(userRepository, "user1", "user1@test.test");
    Long user1Id = user1.getId();
    User user2 = TestUserFactory.createUser(userRepository, "user2", "user2@test.test");
    Long user2Id = user2.getId();

    String now = String.valueOf(System.currentTimeMillis());
    sseService.connect(user1.getId(), now + "-0", null);
    sseService.connect(user2.getId(), now + "-1", null);

    NewClothAttributeEvent event = new NewClothAttributeEvent(1L, "속성 추가");

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

  @DisplayName("동시에 여러 의상 속성 추가 이벤트가 발생할 시 해당 알림들이 sse로 전송된다.")
  @Test
  void handle(CapturedOutput output) throws Exception {
    // given
    String now = String.valueOf(System.currentTimeMillis());
    for(int i = 1; i < 40; i++) {
      User user = TestUserFactory.createUser(userRepository, "user" + i, "user" + i + "@test.test");
      sseService.connect(user.getId(), now + "-" + i, null);
    }

    NewClothAttributeEvent event = new NewClothAttributeEvent(1L, "속성 추가");
    NewClothAttributeEvent event2 = new NewClothAttributeEvent(2L, "속성 추가2");

    CountDownLatch start = new CountDownLatch(1);

    // when
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      Future<?> f1 = executor.submit(() -> { awaitStart(start); listener.handler(event); });
      Future<?> f2 = executor.submit(() -> { awaitStart(start); listener.handler(event2); });

      start.countDown();
      f1.get();
      f2.get();
    }

    // then
    await().untilAsserted(() ->
        assertThat(output.getOut()).contains("의상 속성 추가 이벤트 호출 - ClothingAttributeId=2, Receiver Size=39")
    );
  }

  private static void awaitStart(CountDownLatch latch) {
    try { latch.await(); }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }

}

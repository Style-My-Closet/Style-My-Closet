package com.stylemycloset.sse.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.stylemycloset.IntegrationTestSupport;
import com.stylemycloset.notification.event.domain.NewClothAttributeEvent;
import com.stylemycloset.notification.event.domain.RoleChangedEvent;
import com.stylemycloset.notification.event.listener.NewClothAttributeNotificationEventListener;
import com.stylemycloset.notification.event.listener.RoleChangedNotificationEventListener;
import com.stylemycloset.notification.repository.NotificationRepository;
import com.stylemycloset.notification.util.TestUserFactory;
import com.stylemycloset.sse.service.impl.SseServiceImpl;
import com.stylemycloset.user.entity.Role;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
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
public class RedisSubscriberServiceIntegerTest extends IntegrationTestSupport {

  @Autowired
  NewClothAttributeNotificationEventListener listener;
  @Autowired
  RoleChangedNotificationEventListener roleChangedListener;
  @Autowired
  NotificationRepository notificationRepository;
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
    notificationRepository.deleteAllInBatch();
    try (var connection = connectionFactory.getConnection()) {
      connection.serverCommands().flushDb();
    }
  }

  @DisplayName("동시에 여러 이벤트가 발생할 시 해당 알림들이 중복되지 않고 sse로 전송된다.")
  @Test
  void handle(CapturedOutput output) throws Exception {
    // given
    String now = String.valueOf(System.currentTimeMillis());
    User user1 = TestUserFactory.createUser(userRepository, "user1", "user1@test.test");
    Long user1Id = user1.getId();
    sseService.connect(user1Id, now + "-0", null);

    List<Long> others = new ArrayList<>();
    for(int i = 3; i <= 40; i++) {
      User user = TestUserFactory.createUser(userRepository, "user" + i, "user" + i + "@test.test");
      others.add(user.getId());
      sseService.connect(user.getId(), now + "-" + i, null);
    }

    NewClothAttributeEvent event = new NewClothAttributeEvent(1L, "속성 추가");
    NewClothAttributeEvent event2 = new NewClothAttributeEvent(2L, "속성 추가2");
    RoleChangedEvent event3 = new RoleChangedEvent(user1Id, Role.USER);

    CountDownLatch start = new CountDownLatch(1);

    // when
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      Future<?> f1 = executor.submit(() -> { awaitStart(start); listener.handler(event); });
      Future<?> f2 = executor.submit(() -> { awaitStart(start); listener.handler(event2); });
      Future<?> f3 = executor.submit(() -> { awaitStart(start); roleChangedListener.handler(event3); });

      start.countDown();
      f1.get();
      f2.get();
      f3.get();
    }

    // then
    var user1Records = template.opsForStream().range(NOTIFICATION_KEY + user1Id, Range.unbounded());
    assertThat(user1Records).hasSize(3);
    List<String> ids = user1Records.stream()
            .map(record -> record.getId().getValue()).toList();

    for(Long userId : others) {
      var userRecords = template.opsForStream().range(NOTIFICATION_KEY + userId, Range.unbounded());
      assertThat(userRecords).hasSize(2);
    }

    await().untilAsserted(() -> {
      String logs = output.getOut();
      assertThat(logs).contains("의상 속성 추가 이벤트 호출 - ClothingAttributeId=2, Receiver Size=39");
      assertThat(logs).contains(String.format("사용자 권한 변경 이벤트 호출 - UserId=%d, updatedRole=%s", user1Id, Role.USER));

      ids.forEach(id -> assertThat(logs).contains(String.format("[%d] notifications SSE 이벤트 전송 성공 (eventId: %s)", user1Id, id)));
    });
  }

  private static void awaitStart(CountDownLatch latch) {
    try { latch.await(); }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }
}

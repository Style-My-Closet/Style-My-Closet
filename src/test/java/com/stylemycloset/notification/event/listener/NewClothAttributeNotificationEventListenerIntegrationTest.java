package com.stylemycloset.notification.event.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import com.stylemycloset.notification.entity.Notification;
import com.stylemycloset.notification.event.domain.NewClothAttributeEvent;
import com.stylemycloset.notification.repository.NotificationRepository;
import com.stylemycloset.sse.repository.SseRepository;
import com.stylemycloset.sse.service.impl.SseServiceImpl;
import com.stylemycloset.testutil.IntegrationTestSupport;
import com.stylemycloset.user.dto.request.UserCreateRequest;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public class NewClothAttributeNotificationEventListenerIntegrationTest extends
    IntegrationTestSupport {

  @Autowired
  NewClothAttributeNotificationEventListener listener;

  @MockitoBean
  NotificationRepository notificationRepository;

  @MockitoBean
  UserRepository userRepository;

  @Autowired
  SseServiceImpl sseService;

  @Mock
  SseRepository sseRepository;

  User createUser(String name, String email, Long id) {
    UserCreateRequest request = new UserCreateRequest(name, email, "test");
    User user = new User(request.name(), request.email(), request.password());
    ReflectionTestUtils.setField(user, "id", id);
    return user;
  }

  @DisplayName("의상 속성 추가 이벤트가 호출되면 알림을 생성하고 SSE로 전송 후 로그를 띄운다")
  @Test
  void handleNewClothAttributeEvent_sendSseMessage() throws Exception {
    // given
    User insertUser1 = createUser("insertTest1", "insertTest1@test.test", 2L);
    User insertUser2 = createUser("insertTest2", "insertTest2@test.test", 3L);
    Set<User> users = Set.of(insertUser1, insertUser2);

    String now = String.valueOf(System.currentTimeMillis());
    SseEmitter emitter = sseService.connect(insertUser1.getId(), now, null);
    SseEmitter emitter2 = sseService.connect(insertUser2.getId(), now, null);

    AtomicLong idGenerator = new AtomicLong();

    given(userRepository.findByLockedFalseAndDeleteAtIsNull()).willReturn(users);
    given(notificationRepository.saveAll(anyList()))
        .willAnswer(invocation -> {
          List<Notification> notifications = invocation.getArgument(0);
          Instant createdAt = Instant.now();

          for (Notification notification : notifications) {
            ReflectionTestUtils.setField(notification, "id", idGenerator.getAndIncrement());
            ReflectionTestUtils.setField(notification, "createdAt", createdAt);
          }
          return notifications;
        });
    given(sseRepository.findByUserId(insertUser1.getId())).willReturn(
        new CopyOnWriteArrayList<>(List.of(emitter)));
    given(sseRepository.findByUserId(insertUser2.getId())).willReturn(
        new CopyOnWriteArrayList<>(List.of(emitter2)));

    NewClothAttributeEvent event = new NewClothAttributeEvent(1L, "속성 추가");

    //when
    listener.handler(event);

    // then
    await().untilAsserted(() -> verify(notificationRepository).saveAll(any(List.class)));
  }

}

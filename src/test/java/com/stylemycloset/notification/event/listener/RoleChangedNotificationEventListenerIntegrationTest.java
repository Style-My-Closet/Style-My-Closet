package com.stylemycloset.notification.event.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import com.stylemycloset.notification.TestUserFactory;
import com.stylemycloset.notification.entity.Notification;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.notification.event.domain.RoleChangedEvent;
import com.stylemycloset.notification.repository.NotificationRepository;
import com.stylemycloset.sse.repository.SseRepository;
import com.stylemycloset.sse.service.impl.SseServiceImpl;
import com.stylemycloset.testutil.IntegrationTestSupport;
import com.stylemycloset.user.entity.Role;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public class RoleChangedNotificationEventListenerIntegrationTest extends IntegrationTestSupport {

  @Autowired
  RoleChangedNotificationEventListener listener;

  @MockitoBean
  NotificationRepository notificationRepository;

  @MockitoBean
  UserRepository userRepository;

  @Autowired
  SseServiceImpl sseService;

  @Mock
  SseRepository sseRepository;

  @DisplayName("권한 변경 이벤트 시 알림 저장하고 SSE 전송")
  @Test
  void handler_createsAndSendsNotification() {
    // given
    User user = TestUserFactory.createUser("name", "test@test.email", 1L);
    ReflectionTestUtils.setField(user, "role", Role.USER);

    String now = String.valueOf(System.currentTimeMillis());
    SseEmitter emitter = sseService.connect(user.getId(), now, null);

    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
    given(notificationRepository.save(any(Notification.class)))
        .willAnswer(invocation -> {
          Notification n = invocation.getArgument(0);
          ReflectionTestUtils.setField(n, "id", 1L);
          ReflectionTestUtils.setField(n, "createdAt", Instant.now());
          return n;
        });
    given(sseRepository.findByUserId(user.getId())).willReturn(new CopyOnWriteArrayList<>(List.of(emitter)));

    RoleChangedEvent roleChangedEvent = new RoleChangedEvent(user.getId(), Role.ADMIN);

    // when
    listener.handler(roleChangedEvent);

    // then
    await().untilAsserted(() -> {
      ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
      verify(notificationRepository).save(captor.capture());

      Notification saved = captor.getValue();
      assertThat(saved.getReceiver()).isEqualTo(user);
      assertThat(saved.getTitle()).isEqualTo("내 권한이 변경되었어요.");
      assertThat(saved.getLevel()).isEqualTo(NotificationLevel.INFO);
    });
  }
}

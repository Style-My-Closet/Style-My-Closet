package com.stylemycloset.notification.event.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.verify;

import com.stylemycloset.IntegrationTestSupport;
import com.stylemycloset.notification.entity.Notification;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.notification.event.domain.RoleChangedEvent;
import com.stylemycloset.notification.repository.NotificationRepository;
import com.stylemycloset.notification.util.NotificationStubHelper;
import com.stylemycloset.notification.util.TestUserFactory;
import com.stylemycloset.sse.repository.SseRepository;
import com.stylemycloset.sse.service.impl.SseServiceImpl;
import com.stylemycloset.user.entity.Role;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

  @MockitoBean
  SseRepository sseRepository;

  @DisplayName("권한 변경 이벤트 시 알림 저장하고 SSE 전송")
  @Test
  void handler_createsAndSendsNotification() {
    // given
    User user = TestUserFactory.createUser("definitionName", "test@test.email", 1L);
    ReflectionTestUtils.setField(user, "role", Role.USER);

    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
    NotificationStubHelper.stubSave(notificationRepository);

    Deque<SseEmitter> list1 = new ArrayDeque<>();

    willAnswer(inv -> { list1.add(inv.getArgument(1)); return null; })
        .given(sseRepository).addEmitter(eq(user.getId()), any(SseEmitter.class));
    given(sseRepository.findOrCreateEmitters(user.getId())).willReturn(list1);

    String now = String.valueOf(System.currentTimeMillis());
    sseService.connect(user.getId(), now, null);

    RoleChangedEvent roleChangedEvent = new RoleChangedEvent(user.getId(), Role.ADMIN);

    // when
    listener.handler(roleChangedEvent);

    // then
    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(captor.capture());

    Notification saved = captor.getValue();
    assertThat(saved.getReceiverId()).isEqualTo(user.getId());
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(saved.getTitle()).isEqualTo("내 권한이 변경되었어요.");
    assertThat(saved.getLevel()).isEqualTo(NotificationLevel.INFO);
  }
}

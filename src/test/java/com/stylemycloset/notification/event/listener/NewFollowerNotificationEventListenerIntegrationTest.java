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
import com.stylemycloset.notification.event.domain.FollowEvent;
import com.stylemycloset.notification.repository.NotificationRepository;
import com.stylemycloset.notification.util.NotificationStubHelper;
import com.stylemycloset.notification.util.TestUserFactory;
import com.stylemycloset.sse.repository.SseRepository;
import com.stylemycloset.sse.service.impl.SseServiceImpl;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public class NewFollowerNotificationEventListenerIntegrationTest extends IntegrationTestSupport {

  @Autowired
  NewFollowerNotificationEventListener listener;

  @MockitoBean
  NotificationRepository notificationRepository;

  @MockitoBean
  UserRepository userRepository;

  @Autowired
  SseServiceImpl sseService;

  @MockitoBean
  SseRepository sseRepository;

  @DisplayName("팔로우 이벤트가 호출되면 알림을 생성하고 SSE로 전송 후 로그를 띄운다")
  @Test
  void handleNewFollowerNotificationEvent_sendSseMessage() throws Exception {
    // given
    User followee = TestUserFactory.createUser("followeeUser", "followeeUser@test.test", 15L);

    given(userRepository.findById(followee.getId())).willReturn(Optional.of(followee));
    NotificationStubHelper.stubSave(notificationRepository);

    Deque<SseEmitter> list1 = new ArrayDeque<>();

    willAnswer(inv -> { list1.add(inv.getArgument(1)); return null; })
        .given(sseRepository).addEmitter(eq(followee.getId()), any(SseEmitter.class));
    given(sseRepository.findOrCreateEmitters(followee.getId())).willReturn(list1);

    String now = String.valueOf(System.currentTimeMillis());
    sseService.connect(followee.getId(), now, null);

    FollowEvent followEvent = new FollowEvent(followee.getId(), "user");

    // when
    listener.handler(followEvent);

    // then
    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(captor.capture());

    Notification saved = captor.getValue();
    assertThat(saved.getReceiverId()).isEqualTo(followee.getId());
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(saved.getTitle()).isEqualTo("user님이 나를 팔로우했어요.");
    assertThat(saved.getLevel()).isEqualTo(NotificationLevel.INFO);
  }
}

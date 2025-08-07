package com.stylemycloset.notification.event.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import com.stylemycloset.notification.entity.Notification;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.notification.event.domain.FollowEvent;
import com.stylemycloset.notification.repository.NotificationRepository;
import com.stylemycloset.notification.util.NotificationStubHelper;
import com.stylemycloset.notification.util.TestUserFactory;
import com.stylemycloset.sse.repository.SseRepository;
import com.stylemycloset.sse.service.impl.SseServiceImpl;
import com.stylemycloset.testutil.IntegrationTestSupport;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
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

  @Mock
  SseRepository sseRepository;

  @DisplayName("팔로우 이벤트가 호출되면 알림을 생성하고 SSE로 전송 후 로그를 띄운다")
  @Test
  void handleNewFollowerNotificationEvent_sendSseMessage() throws Exception {
    // given
    User followee = TestUserFactory.createUser("followeeUser", "followeeUser@test.test", 15L);

    String now = String.valueOf(System.currentTimeMillis());
    SseEmitter emitter = sseService.connect(followee.getId(), now, null);

    given(userRepository.findById(followee.getId())).willReturn(Optional.of(followee));
    NotificationStubHelper.stubSave(notificationRepository);
    given(sseRepository.findByUserId(followee.getId())).willReturn(new CopyOnWriteArrayList<>(List.of(emitter)));

    FollowEvent followEvent = new FollowEvent(followee.getId(), "user");

    // when
    listener.handler(followEvent);

    // then
    await().untilAsserted(() -> {
      ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
      verify(notificationRepository).save(captor.capture());

      Notification saved = captor.getValue();
      assertThat(saved.getReceiver()).isEqualTo(followee);
      assertThat(saved.getTitle()).isEqualTo("user님이 나를 팔로우했어요.");
      assertThat(saved.getLevel()).isEqualTo(NotificationLevel.INFO);
    });
  }
}

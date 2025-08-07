package com.stylemycloset.notification.event.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import com.stylemycloset.notification.entity.Notification;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.notification.event.domain.FeedCommentEvent;
import com.stylemycloset.notification.repository.NotificationRepository;
import com.stylemycloset.sse.repository.SseRepository;
import com.stylemycloset.sse.service.impl.SseServiceImpl;
import com.stylemycloset.testutil.IntegrationTestSupport;
import com.stylemycloset.user.dto.request.UserCreateRequest;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public class FeedCommentNotificationEventListenerIntegrationTest extends IntegrationTestSupport {

  @Autowired
  FeedCommentNotificationEventListener listener;

  @MockitoBean
  NotificationRepository notificationRepository;

  @MockitoBean
  UserRepository userRepository;

  @Autowired
  SseServiceImpl sseService;

  @Mock
  SseRepository sseRepository;

  @DisplayName("피드 댓글 이벤트가 호출되면 알림을 생성하고 SSE로 전송 후 로그를 띄운다")
  @Test
  void handleCommentEvent_sendSseMessage() throws Exception {
    // given
    UserCreateRequest request = new UserCreateRequest("name", "test@test.email", "test");
    User user = new User(request);
    ReflectionTestUtils.setField(user, "id", 7L);

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
    given(sseRepository.findByUserId(user.getId())).willReturn(List.of(emitter));

    FeedCommentEvent event = new FeedCommentEvent(1L, "user2", "피드 좋아요 테스트", user.getId());

    // when
    listener.handler(event);

    // then
    await().untilAsserted(() -> {
      ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
      verify(notificationRepository).save(captor.capture());

      Notification saved = captor.getValue();
      assertThat(saved.getReceiver()).isEqualTo(user);
      assertThat(saved.getTitle()).isEqualTo("user2님이 댓글을 달았어요.");
      assertThat(saved.getLevel()).isEqualTo(NotificationLevel.INFO);
    });
  }
}

package com.stylemycloset.notification.event.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import com.stylemycloset.notification.TestUserFactory;
import com.stylemycloset.notification.entity.Notification;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.notification.event.domain.FeedLikedEvent;
import com.stylemycloset.notification.repository.NotificationRepository;
import com.stylemycloset.ootd.entity.Feed;
import com.stylemycloset.ootd.repo.FeedRepository;
import com.stylemycloset.sse.repository.SseRepository;
import com.stylemycloset.sse.service.impl.SseServiceImpl;
import com.stylemycloset.testutil.IntegrationTestSupport;
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

public class FeedLikedNotificationEventListenerIntegrationTest extends IntegrationTestSupport {

  @Autowired
  FeedLikedNotificationEventListener listener;

  @MockitoBean
  NotificationRepository notificationRepository;

  @MockitoBean
  UserRepository userRepository;

  @MockitoBean
  FeedRepository feedRepository;

  @Autowired
  SseServiceImpl sseService;

  @Mock
  SseRepository sseRepository;

  @DisplayName("피드 좋아요 이벤트가 호출되면 알림을 생성하고 SSE로 전송 후 로그를 띄운다")
  @Test
  void handleFeedLikeEvent_sendSseMessage() throws Exception {
    // given
    User user = TestUserFactory.createUser("name", "test@test.email", 6L);
    User likeUser = TestUserFactory.createUser("likeUsername", "test@test.email", 66L);

    Feed feed = Feed.createFeed(user, null, "피드 내용");
    ReflectionTestUtils.setField(feed, "id", 6L);

    String now = String.valueOf(System.currentTimeMillis());
    SseEmitter emitter = sseService.connect(user.getId(), now, null);

    given(feedRepository.findWithUserById(feed.getId())).willReturn(Optional.of(feed));
    given(userRepository.findById(likeUser.getId())).willReturn(Optional.of(likeUser));
    given(notificationRepository.save(any(Notification.class)))
        .willAnswer(invocation -> {
          Notification n = invocation.getArgument(0);
          ReflectionTestUtils.setField(n, "id", 1L);
          ReflectionTestUtils.setField(n, "createdAt", Instant.now());
          return n;
        });
    given(sseRepository.findByUserId(user.getId())).willReturn(new CopyOnWriteArrayList<>(List.of(emitter)));

    FeedLikedEvent event = new FeedLikedEvent(6L, 66L);

    // when
    listener.handler(event);

    // then
    await().untilAsserted(() -> {
      ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
      verify(notificationRepository).save(captor.capture());

      Notification saved = captor.getValue();
      assertThat(saved.getReceiver()).isEqualTo(user);
      assertThat(saved.getTitle()).isEqualTo("likeUsername님이 내 피드를 좋아합니다.");
      assertThat(saved.getLevel()).isEqualTo(NotificationLevel.INFO);
    });
  }
}

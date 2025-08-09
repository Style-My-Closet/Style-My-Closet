package com.stylemycloset.notification.event.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.stylemycloset.notification.entity.Notification;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.notification.event.domain.FeedCommentEvent;
import com.stylemycloset.notification.repository.NotificationRepository;
import com.stylemycloset.notification.util.NotificationStubHelper;
import com.stylemycloset.notification.util.TestUserFactory;
import com.stylemycloset.ootd.entity.Feed;
import com.stylemycloset.ootd.repo.FeedRepository;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public class FeedCommentNotificationEventListenerIntegrationTest extends IntegrationTestSupport {

  @Autowired
  FeedCommentNotificationEventListener listener;

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

  @DisplayName("피드 댓글 이벤트가 호출되면 알림을 생성하고 SSE로 전송 후 로그를 띄운다")
  @Test
  void handleCommentEvent_sendSseMessage() throws Exception {
    // given
    User user = TestUserFactory.createUser("name", "test@test.email", 7L);
    User commentUser = TestUserFactory.createUser("commentUsername", "test@test.email", 77L);

    Feed feed = Feed.createFeed(user, null, "피드 내용");
    ReflectionTestUtils.setField(feed, "id", 7L);

    String now = String.valueOf(System.currentTimeMillis());
    SseEmitter emitter = sseService.connect(user.getId(), now, null);

    given(feedRepository.findWithUserById(feed.getId())).willReturn(Optional.of(feed));
    given(userRepository.findById(commentUser.getId())).willReturn(Optional.of(commentUser));
    NotificationStubHelper.stubSave(notificationRepository);
    given(sseRepository.findOrCreateEmitters(user.getId())).willReturn(new CopyOnWriteArrayList<>(List.of(emitter)));

    FeedCommentEvent event = new FeedCommentEvent(7L, 77L);

    // when
    listener.handler(event);

    // then
    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(captor.capture());

    Notification saved = captor.getValue();
    assertThat(saved.getReceiver()).isEqualTo(user);
    assertThat(saved.getTitle()).isEqualTo("commentUsername님이 댓글을 달았어요.");
    assertThat(saved.getLevel()).isEqualTo(NotificationLevel.INFO);
  }
}

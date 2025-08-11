package com.stylemycloset.notification.event.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.verify;

import com.stylemycloset.follow.entity.repository.FollowRepository;
import com.stylemycloset.notification.event.domain.NewFeedEvent;
import com.stylemycloset.notification.repository.NotificationRepository;
import com.stylemycloset.notification.util.NotificationStubHelper;
import com.stylemycloset.notification.util.TestUserFactory;
import com.stylemycloset.sse.dto.SseInfo;
import com.stylemycloset.sse.repository.SseRepository;
import com.stylemycloset.sse.service.impl.SseServiceImpl;
import com.stylemycloset.testutil.IntegrationTestSupport;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import java.util.Deque;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public class NewFeedNotificationEventListenerIntegrationTest extends IntegrationTestSupport {

  @Autowired
  NewFeedNotificationEventListener listener;

  @MockitoBean
  NotificationRepository notificationRepository;

  @MockitoBean
  UserRepository userRepository;

  @MockitoBean
  FollowRepository followRepository;

  @Autowired
  SseServiceImpl sseService;

  @MockitoBean
  SseRepository sseRepository;

  @DisplayName("새로운 피드 생성 추가 이벤트가 호출되면 알림을 생성하고 SSE로 전송 후 로그를 띄운다")
  @Test
  void handleNewFeedNotificationEvent_sendSseMessage() throws Exception {
    // given
    User feedAuthor = TestUserFactory.createUser("feedAuthor", "feedAuthor@test.test", 18L);
    User follower1 = TestUserFactory.createUser("follower1", "follower1@test.test", 180L);
    User follower2 = TestUserFactory.createUser("follower2", "follower2@test.test", 1800L);
    Set<Long> receivers = Set.of(follower1.getId(), follower2.getId());

    given(userRepository.findById(feedAuthor.getId())).willReturn(Optional.of(feedAuthor));
    given(followRepository.findFollowerIdsByFolloweeId(feedAuthor.getId())).willReturn(receivers);
    NotificationStubHelper.stubSaveAll(notificationRepository);

    CopyOnWriteArrayList<SseEmitter> list1 = new CopyOnWriteArrayList<>();
    CopyOnWriteArrayList<SseEmitter> list2 = new CopyOnWriteArrayList<>();
    given(sseRepository.findOrCreateEmitters(follower1.getId())).willReturn(list1);
    given(sseRepository.findOrCreateEmitters(follower2.getId())).willReturn(list2);
    willAnswer(inv -> { list1.add(inv.getArgument(1)); return null; })
        .given(sseRepository).addEmitter(eq(follower1.getId()), any(SseEmitter.class));
    willAnswer(inv -> { list2.add(inv.getArgument(1)); return null; })
        .given(sseRepository).addEmitter(eq(follower2.getId()), any(SseEmitter.class));

    Deque<SseInfo> queue1 = new ConcurrentLinkedDeque<>();
    Deque<SseInfo> queue2 = new ConcurrentLinkedDeque<>();
    given(sseRepository.findOrCreateEvents(follower1.getId())).willReturn(queue1);
    given(sseRepository.findOrCreateEvents(follower2.getId())).willReturn(queue2);

    String now = String.valueOf(System.currentTimeMillis());
    sseService.connect(follower1.getId(), now, null);
    sseService.connect(follower2.getId(), now, null);

    NewFeedEvent event = new NewFeedEvent(1L, "피드 테스트", feedAuthor.getId());

    // when
    listener.handler(event);

    // then
    verify(notificationRepository).saveAll(anyList());
    assertThat(queue1).isNotEmpty();
    assertThat(queue2).isNotEmpty();
  }
}

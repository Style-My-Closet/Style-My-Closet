package com.stylemycloset.notification.event.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.verify;

import com.stylemycloset.notification.event.domain.ClothAttributeChangedEvent;
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
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public class ClothAttributeChangedNotificationEventListenerIntegrationTest extends
    IntegrationTestSupport {

  @Autowired
  ClothAttributeChangedNotificationEventListener listener;

  @MockitoBean
  NotificationRepository notificationRepository;

  @MockitoBean
  UserRepository userRepository;

  @Autowired
  SseServiceImpl sseService;

  @MockitoBean
  SseRepository sseRepository;

  @DisplayName("의상 속성 변경 이벤트가 호출되면 알림을 생성하고 SSE로 전송 후 로그를 띄운다")
  @Test
  void handleClothAttributeChangedEvent_sendSseMessage() throws Exception {
    // given
    User changedUser1 = TestUserFactory.createUser("ChangedUser1", "ChangedUser1@test.test", 4L);
    User changedUser2 = TestUserFactory.createUser("ChangedUser2", "ChangedUser2@test.test", 5L);
    Set<Long> users = Set.of(changedUser1.getId(), changedUser2.getId());

    given(userRepository.findActiveUserIds()).willReturn(users);
    NotificationStubHelper.stubSaveAll(notificationRepository);

    CopyOnWriteArrayList<SseEmitter> list1 = new CopyOnWriteArrayList<>();
    CopyOnWriteArrayList<SseEmitter> list2 = new CopyOnWriteArrayList<>();
    given(sseRepository.findOrCreateEmitters(changedUser1.getId())).willReturn(list1);
    given(sseRepository.findOrCreateEmitters(changedUser2.getId())).willReturn(list2);
    willAnswer(inv -> { list1.add(inv.getArgument(1)); return null; })
        .given(sseRepository).addEmitter(eq(changedUser1.getId()), any(SseEmitter.class));
    willAnswer(inv -> { list2.add(inv.getArgument(1)); return null; })
        .given(sseRepository).addEmitter(eq(changedUser2.getId()), any(SseEmitter.class));

    Deque<SseInfo> queue1 = new ConcurrentLinkedDeque<>();
    Deque<SseInfo> queue2 = new ConcurrentLinkedDeque<>();
    given(sseRepository.findOrCreateEvents(changedUser1.getId())).willReturn(queue1);
    given(sseRepository.findOrCreateEvents(changedUser2.getId())).willReturn(queue2);

    String now = String.valueOf(System.currentTimeMillis());
    sseService.connect(changedUser1.getId(), now, null);
    sseService.connect(changedUser2.getId(), now, null);

    ClothAttributeChangedEvent event = new ClothAttributeChangedEvent(1L, "속성 변경");

    //when
    listener.handler(event);

    // then
    verify(notificationRepository).saveAll(anyList());
    assertThat(queue1).isNotEmpty();
    assertThat(queue2).isNotEmpty();
  }
}

package com.stylemycloset.notification.event.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.verify;

import com.stylemycloset.IntegrationTestSupport;
import com.stylemycloset.notification.event.domain.NewClothAttributeEvent;
import com.stylemycloset.notification.repository.NotificationRepository;
import com.stylemycloset.notification.util.NotificationStubHelper;
import com.stylemycloset.notification.util.TestUserFactory;
import com.stylemycloset.sse.repository.SseRepository;
import com.stylemycloset.sse.service.impl.SseServiceImpl;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public class NewClothAttributeNotificationEventListenerIntegrationTest extends IntegrationTestSupport {

  @Autowired
  NewClothAttributeNotificationEventListener listener;

  @MockitoBean
  NotificationRepository notificationRepository;

  @MockitoBean
  UserRepository userRepository;

  @Autowired
  SseServiceImpl sseService;

  @MockitoBean
  SseRepository sseRepository;

  @DisplayName("의상 속성 추가 이벤트가 호출되면 알림을 생성하고 SSE로 전송 후 로그를 띄운다")
  @Test
  void handleNewClothAttributeEvent_sendSseMessage() throws Exception {
    // given
    User insertUser1 = TestUserFactory.createUser("insertTest1", "insertTest1@test.test", 2L);
    User insertUser2 = TestUserFactory.createUser("insertTest2", "insertTest2@test.test", 3L);
    Set<Long> users = Set.of(insertUser1.getId(), insertUser2.getId());

    given(userRepository.findActiveUserIds()).willReturn(users);
    NotificationStubHelper.stubSaveAll(notificationRepository);

    Deque<SseEmitter> list1 = new ArrayDeque<>();
    Deque<SseEmitter> list2 = new ArrayDeque<>();

    willAnswer(inv -> { list1.add(inv.getArgument(1)); return null; })
        .given(sseRepository).addEmitter(eq(insertUser1.getId()), any(SseEmitter.class));
    willAnswer(inv -> { list2.add(inv.getArgument(1)); return null; })
        .given(sseRepository).addEmitter(eq(insertUser2.getId()), any(SseEmitter.class));

    given(sseRepository.findOrCreateEmitters(insertUser1.getId())).willReturn(list1);
    given(sseRepository.findOrCreateEmitters(insertUser2.getId())).willReturn(list2);

    String now = String.valueOf(System.currentTimeMillis());
    sseService.connect(insertUser1.getId(), now, null);
    sseService.connect(insertUser2.getId(), now, null);

    NewClothAttributeEvent event = new NewClothAttributeEvent(1L, "속성 추가");

    //when
    listener.handler(event);

    // then
    verify(notificationRepository).saveAll(anyList());
  }

}

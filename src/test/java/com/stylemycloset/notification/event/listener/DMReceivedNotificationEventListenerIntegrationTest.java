package com.stylemycloset.notification.event.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.stylemycloset.directmessage.entity.Message;
import com.stylemycloset.directmessage.entity.repository.MessageRepository;
import com.stylemycloset.notification.entity.Notification;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.notification.event.domain.DMSentEvent;
import com.stylemycloset.notification.repository.NotificationRepository;
import com.stylemycloset.notification.util.NotificationStubHelper;
import com.stylemycloset.notification.util.TestUserFactory;
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

public class DMReceivedNotificationEventListenerIntegrationTest extends IntegrationTestSupport {

  @Autowired
  DMReceivedNotificationEventListener listener;

  @MockitoBean
  NotificationRepository notificationRepository;

  @MockitoBean
  UserRepository userRepository;

  @MockitoBean
  MessageRepository messageRepository;

  @Autowired
  SseServiceImpl sseService;

  @Mock
  SseRepository sseRepository;

  @DisplayName("DM 이벤트가 호출되면 알림을 생성하고 SSE로 전송 후 로그를 띄운다")
  @Test
  void handleDMReceivedNotificationEvent_sendSseMessage() throws Exception {
    // given
    User dmSender = TestUserFactory.createUser("dmSenderUser", "dmSenderUser@test.test", 16L);
    User dmReceiver = TestUserFactory.createUser("dmReceiverUser", "dmReceiverUser@test.test", 160L);
    Message message = new Message(dmSender, dmReceiver, "test", Instant.now());
    ReflectionTestUtils.setField(message, "id", 1L);

    String now = String.valueOf(System.currentTimeMillis());
    SseEmitter emitter = sseService.connect(dmReceiver.getId(), now, null);

    given(messageRepository.findWithReceiverById(message.getId())).willReturn(Optional.of(message));
    NotificationStubHelper.stubSave(notificationRepository);
    given(sseRepository.findOrCreateEmitters(dmReceiver.getId())).willReturn(new CopyOnWriteArrayList<>(List.of(emitter)));

    DMSentEvent dmSentEvent = new DMSentEvent(message.getId(), "user");

    // when
    listener.handler(dmSentEvent);

    // then
    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(captor.capture());

    Notification saved = captor.getValue();
    assertThat(saved.getReceiver()).isEqualTo(dmReceiver);
    assertThat(saved.getTitle()).isEqualTo("[DM] user");
    assertThat(saved.getLevel()).isEqualTo(NotificationLevel.INFO);
  }

}

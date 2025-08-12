package com.stylemycloset.notification.event.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.verify;

import com.stylemycloset.notification.entity.Notification;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.notification.event.domain.WeatherAlertEvent;
import com.stylemycloset.notification.repository.NotificationRepository;
import com.stylemycloset.notification.util.NotificationStubHelper;
import com.stylemycloset.notification.util.TestUserFactory;
import com.stylemycloset.sse.dto.SseInfo;
import com.stylemycloset.sse.repository.SseRepository;
import com.stylemycloset.sse.service.impl.SseServiceImpl;
import com.stylemycloset.IntegrationTestSupport;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public class WeatherAlertNotificationEventListenerIntegrationTest extends IntegrationTestSupport {

  @Autowired
  WeatherAlertNotificationEventListener listener;

  @MockitoBean
  NotificationRepository notificationRepository;

  @MockitoBean
  UserRepository userRepository;

  @Autowired
  SseServiceImpl sseService;

  @MockitoBean
  SseRepository sseRepository;

  @DisplayName("날씨 변화 이벤트가 호출되면 알림을 생성하고 SSE로 전송 후 로그를 띄운다")
  @Test
  void handleWeatherAlertNotificationEvent_sendSseMessage() throws Exception {
    // given
    User weatherSender = TestUserFactory.createUser("weatherSender", "weatherSender@test.test", 17L);

    given(userRepository.findById(weatherSender.getId())).willReturn(Optional.of(weatherSender));
    NotificationStubHelper.stubSave(notificationRepository);

    CopyOnWriteArrayList<SseEmitter> list1 = new CopyOnWriteArrayList<>();
    given(sseRepository.findOrCreateEmitters(weatherSender.getId())).willReturn(list1);
    willAnswer(inv -> { list1.add(inv.getArgument(1)); return null; })
        .given(sseRepository).addEmitter(eq(weatherSender.getId()), any(SseEmitter.class));

    Deque<SseInfo> queue1 = new ConcurrentLinkedDeque<>();
    given(sseRepository.findOrCreateEvents(weatherSender.getId())).willReturn(queue1);

    String now = String.valueOf(System.currentTimeMillis());
    sseService.connect(weatherSender.getId(), now, null);

    WeatherAlertEvent weatherAlertEvent = new WeatherAlertEvent(weatherSender.getId(), 1L, "날씨 변화 테스트");

    // when
    listener.handler(weatherAlertEvent);

    // then
    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(captor.capture());

    Notification saved = captor.getValue();
    assertThat(saved.getReceiverId()).isEqualTo(weatherSender.getId());
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(saved.getTitle()).isEqualTo("급격한 날씨 변화가 발생했습니다.");
    assertThat(saved.getContent()).isEqualTo("날씨 변화 테스트");
    assertThat(saved.getLevel()).isEqualTo(NotificationLevel.WARNING);
    assertThat(queue1).isNotEmpty();
  }
}

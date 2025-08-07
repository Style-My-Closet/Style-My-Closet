package com.stylemycloset.notification.event.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import com.stylemycloset.notification.entity.Notification;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.notification.event.domain.WeatherAlertEvent;
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

public class WeatherAlertNotificationEventListenerIntegrationTest extends IntegrationTestSupport {

  @Autowired
  WeatherAlertNotificationEventListener listener;

  @MockitoBean
  NotificationRepository notificationRepository;

  @MockitoBean
  UserRepository userRepository;

  @Autowired
  SseServiceImpl sseService;

  @Mock
  SseRepository sseRepository;

  @DisplayName("날씨 변화 이벤트가 호출되면 알림을 생성하고 SSE로 전송 후 로그를 띄운다")
  @Test
  void handleWeatherAlertNotificationEvent_sendSseMessage() throws Exception {
    // given
    User weatherSender = TestUserFactory.createUser("weatherSender", "weatherSender@test.test", 17L);

    String now = String.valueOf(System.currentTimeMillis());
    SseEmitter emitter = sseService.connect(weatherSender.getId(), now, null);

    given(userRepository.findById(weatherSender.getId())).willReturn(Optional.of(weatherSender));
    NotificationStubHelper.stubSave(notificationRepository);
    given(sseRepository.findByUserId(weatherSender.getId())).willReturn(new CopyOnWriteArrayList<>(
        List.of(emitter)));

    WeatherAlertEvent weatherAlertEvent = new WeatherAlertEvent(weatherSender.getId(), 1L, "날씨 변화 테스트");

    // when
    listener.handler(weatherAlertEvent);

    // then
    await().untilAsserted(() -> {
      ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
      verify(notificationRepository).save(captor.capture());

      Notification saved = captor.getValue();
      assertThat(saved.getReceiver()).isEqualTo(weatherSender);
      assertThat(saved.getTitle()).isEqualTo("급격한 날씨 변화가 발생했습니다.");
      assertThat(saved.getContent()).isEqualTo("날씨 변화 테스트");
      assertThat(saved.getLevel()).isEqualTo(NotificationLevel.WARNING);
    });
  }
}

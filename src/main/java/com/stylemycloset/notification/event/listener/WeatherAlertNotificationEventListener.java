package com.stylemycloset.notification.event.listener;

import com.stylemycloset.notification.dto.NotificationDto;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.notification.event.NotificationStreamPublisher;
import com.stylemycloset.notification.event.domain.WeatherAlertEvent;
import com.stylemycloset.notification.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class WeatherAlertNotificationEventListener {

  private final NotificationService notificationService;
  private final NotificationStreamPublisher streamPublisher;

  private static final String WEATHER_TITLE = "급격한 날씨 변화가 발생했습니다.";

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handler(WeatherAlertEvent event) {
    log.info("날씨 변화 이벤트 호출 - weatherId={}, receiverId={}", event.weatherId(), event.receiverId());
    try{
      NotificationDto dto = notificationService.create(event.receiverId(), WEATHER_TITLE, event.message(), NotificationLevel.WARNING);
      streamPublisher.processAndPublish(List.of(dto));
    } catch(Exception e){
      log.error("날씨 변화 이벤트 처리 중 예외 발생 - weatherId={}, receiverId={}", event.weatherId(), event.receiverId(), e);
    }
  }
}

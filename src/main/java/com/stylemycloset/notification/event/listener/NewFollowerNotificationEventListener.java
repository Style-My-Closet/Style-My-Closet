package com.stylemycloset.notification.event.listener;

import com.stylemycloset.notification.dto.NotificationDto;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.notification.event.NotificationStreamPublisher;
import com.stylemycloset.notification.event.domain.FollowEvent;
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
public class NewFollowerNotificationEventListener {

  private final NotificationService notificationService;
  private final NotificationStreamPublisher streamPublisher;

  private static final String NEW_FOLLOW = "%s님이 나를 팔로우했어요.";

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handler(FollowEvent event) {
    log.info("팔로우 이벤트 호출 - receiverId={}, followUsername={}", event.receiverId(),
        event.followUsername());

    try {
      String title = String.format(NEW_FOLLOW, event.followUsername());
      NotificationDto dto = notificationService.create(event.receiverId(), title, "",
          NotificationLevel.INFO);

      streamPublisher.processAndPublish(List.of(dto));
    } catch (Exception e) {
      log.error("팔로우 이벤트 처리 중 예외 발생 - receiverId={}, followUsername={}", event.receiverId(),
          event.followUsername(), e);
    }
  }
}

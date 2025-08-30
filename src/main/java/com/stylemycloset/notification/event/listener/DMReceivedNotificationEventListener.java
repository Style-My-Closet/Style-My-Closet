package com.stylemycloset.notification.event.listener;

import com.stylemycloset.directmessage.entity.DirectMessage;
import com.stylemycloset.directmessage.exception.DirectMessageNotFoundException;
import com.stylemycloset.directmessage.repository.DirectMessageRepository;
import com.stylemycloset.notification.dto.NotificationDto;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.notification.event.NotificationStreamPublisher;
import com.stylemycloset.notification.event.domain.DMSentEvent;
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
public class DMReceivedNotificationEventListener {

  private final NotificationService notificationService;
  private final DirectMessageRepository messageRepository;
  private final NotificationStreamPublisher streamPublisher;

  private static final String NEW_MESSAGE = "[DM] %s";

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handler(DMSentEvent event) {
    log.info("DM 이벤트 호출 - messageId={}", event.messageId());

    DirectMessage message = messageRepository.findWithReceiverById(event.messageId())
        .orElseThrow(() -> new DirectMessageNotFoundException(event.messageId()));
    try {
      String title = String.format(NEW_MESSAGE, event.sendUsername());
      NotificationDto dto = notificationService.create(message.getReceiver().getId(),
          title, message.getContent(), NotificationLevel.INFO);

      streamPublisher.processAndPublish(List.of(dto));
    } catch (Exception e) {
      log.error("DM 이벤트 처리 중 예외 발생 - messageId={}", event.messageId(), e);
    }
  }

}

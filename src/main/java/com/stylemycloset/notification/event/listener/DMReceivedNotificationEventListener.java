package com.stylemycloset.notification.event.listener;

import com.stylemycloset.directmessage.entity.Message;
import com.stylemycloset.directmessage.entity.exception.MessageNotFoundException;
import com.stylemycloset.directmessage.entity.repository.MessageRepository;
import com.stylemycloset.notification.dto.NotificationDto;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.notification.event.domain.DMSentEvent;
import com.stylemycloset.notification.service.NotificationService;
import com.stylemycloset.sse.service.SseService;
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
  private final SseService sseService;
  private final MessageRepository messageRepository;

  private static final String NEW_MESSAGE = "[DM] %s";

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handler(DMSentEvent event) {
    log.info("DM 이벤트 호출 - messageId={}", event.messageId());

    Message message = messageRepository.findWithReceiverById(event.messageId())
        .orElseThrow(() -> new MessageNotFoundException(event.messageId()));
    try {
      String title = String.format(NEW_MESSAGE, event.sendUsername());
      NotificationDto notificationDto = notificationService.create(message.getReceiver().getId(), title, "", NotificationLevel.INFO);

      sseService.sendNotification(notificationDto);
      log.info("DM 이벤트 완료 - notificationId={}",  notificationDto.id());
    } catch (Exception e) {
      log.error("DM 이벤트 처리 중 예외 발생 - messageId={}", event.messageId(), e);
    }
  }

}

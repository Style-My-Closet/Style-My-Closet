package com.stylemycloset.notification.event.listener;

import com.stylemycloset.notification.dto.NotificationDto;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.notification.event.domain.ClothAttributeChangedEvent;
import com.stylemycloset.notification.service.NotificationService;
import com.stylemycloset.sse.service.SseService;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class ClothAttributeChangedNotificationEventListener  {

  private final NotificationService notificationService;
  private final SseService sseService;
  private final UserRepository userRepository;

  private static final String CLOTH_ATTRIBUTE_CHANGED = "의상 속성이 변경되었어요.";
  private static final String CLOTH_ATTRIBUTE_CHANGED_CONTENT = "[%s] 속성을 확인해보세요.";

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handler(ClothAttributeChangedEvent event) {
    Set<User> receivers = userRepository.findByLockedFalseAndDeleteAtIsNull();
    log.info("의상 속성 변경 이벤트 호출 - ClothingAttributeId={}, Receiver Size={}",
        event.clothAttributeId(), receivers.size());

    String content = String.format(CLOTH_ATTRIBUTE_CHANGED_CONTENT, event.changedAttributeName());
    List<NotificationDto> notificationDtoList =
        notificationService.createAll(receivers, CLOTH_ATTRIBUTE_CHANGED, content, NotificationLevel.INFO);

    for(NotificationDto notificationDto : notificationDtoList){
      sseService.sendNotification(notificationDto);
    }
    log.info("의상 속성 변경 이벤트 완료 - notification Size={}", notificationDtoList.size());
  }
}

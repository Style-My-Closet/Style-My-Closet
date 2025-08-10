package com.stylemycloset.notification.event.listener;

import com.stylemycloset.notification.dto.NotificationDto;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.notification.event.domain.NewClothAttributeEvent;
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
public class NewClothAttributeNotificationEventListener {

  private final NotificationService notificationService;
  private final SseService sseService;
  private final UserRepository userRepository;

  private static final String NEW_CLOTH_ATTRIBUTE = "새로운 의상 속성이 추가되었어요.";
  private static final String NEW_CLOTH_ATTRIBUTE_CONTENT = "내 의상에 [%s] 속성을 추가해보세요.";

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handler(NewClothAttributeEvent event) {
    try {
      Set<User> receivers = userRepository.findByLockedFalseAndDeleteAtIsNull();
      log.info("의상 속성 추가 이벤트 호출 - ClothingAttributeId={}, Receiver Size={}",
          event.clothAttributeId(), receivers.size());

      String content = String.format(NEW_CLOTH_ATTRIBUTE_CONTENT, event.attributeName());
      List<NotificationDto> notificationDtoList =
          notificationService.createAll(receivers, NEW_CLOTH_ATTRIBUTE, content,
              NotificationLevel.INFO);

      for (NotificationDto notificationDto : notificationDtoList) {
        sseService.sendNotification(notificationDto);
      }
      log.info("의상 속성 추가 이벤트 완료 - notification Size={}", notificationDtoList.size());
    } catch (Exception e) {
      log.error("의상 속성 추가 이벤트 처리 중 예외 발생 - clothAttributeId={}", event.clothAttributeId(), e);
    }
  }
}

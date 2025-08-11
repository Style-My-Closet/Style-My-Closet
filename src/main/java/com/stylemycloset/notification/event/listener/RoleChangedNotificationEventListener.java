package com.stylemycloset.notification.event.listener;

import com.stylemycloset.notification.dto.NotificationDto;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.notification.event.domain.RoleChangedEvent;
import com.stylemycloset.notification.service.NotificationService;
import com.stylemycloset.sse.service.SseService;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.exception.UserNotFoundException;
import com.stylemycloset.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class RoleChangedNotificationEventListener {

  private final NotificationService notificationService;
  private final SseService sseService;
  private final UserRepository userRepository;

  private static final String ROLE_CHANGED = "내 권한이 변경되었어요.";
  private static final String ROLE_CHANGED_CONTENT = "내 권한이 [%s]에서 [%s]로 변경되었어요.";

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handler(RoleChangedEvent event) {
    User receiver = userRepository.findById(event.receiverId())
        .orElseThrow(UserNotFoundException::new);
    log.info("사용자 권한 변경 이벤트 호출 - UserId={}, updatedRole={} ", receiver.getId(),
        receiver.getRole());
    try {
      String content = String.format(ROLE_CHANGED_CONTENT, event.previousRole(),
          receiver.getRole());
      NotificationDto notificationDto = notificationService.create(event.receiverId(), ROLE_CHANGED, content,
          NotificationLevel.INFO);

      sseService.sendNotification(notificationDto);
      log.info("사용자 권한 변경 이벤트 완료 - notificationId={}", notificationDto.id());
    } catch (Exception e) {
      log.error("사용자 권한 이벤트 처리 중 예외 발생 - receiverId={}", event.receiverId(), e);
    }
  }
}

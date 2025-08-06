package com.stylemycloset.notification.event.listener;

import com.stylemycloset.notification.dto.NotificationDto;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.notification.event.ClothAttributeChangedEvent;
import com.stylemycloset.notification.event.FeedCommentEvent;
import com.stylemycloset.notification.event.FeedLikedEvent;
import com.stylemycloset.notification.event.NewClothAttributeEvent;
import com.stylemycloset.notification.event.RoleChangedEvent;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

  private final NotificationService notificationService;
  private final UserRepository userRepository;
  private final SseService sseService;

  private static final String ROLE_CHANGED = "내 권한이 변경되었어요.";
  private static final String ROLE_CHANGED_CONTENT = "내 권한이 [%s]에서 [%s]로 변경되었어요.";

  private static final String NEW_CLOTH_ATTRIBUTE = "새로운 의상 속성이 추가되었어요.";
  private static final String NEW_CLOTH_ATTRIBUTE_CONTENT = "내 의상에 [%s] 속성을 추가해보세요.";

  private static final String CLOTH_ATTRIBUTE_CHANGED = "의상 속성이 변경되었어요.";
  private static final String CLOTH_ATTRIBUTE_CHANGED_CONTENT = "[%s] 속성을 확인해보세요.";

  private static final String NEW_COMMENT = "%s님이 댓글을 달았어요.";
  private static final String FEED_LIKED = "%s님이 내 피드를 좋아합니다.";

  private static final String NEW_FOLLOW = "%s님이 나를 팔로우했어요.";
  private static final String NEW_MESSAGE = "[DM] %s";
  private static final String FEED_ADDED = "%s님이 새로운 피드를 작성했어요.";
  private static final String WEATHER_EVENT = "오늘 날씨에 주의하세요";

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleRoleChangedEvent(RoleChangedEvent event) {
    User user = event.user();
    log.info("사용자 권한 변경 이벤트 호출 - UserId={}, updatedRole={} ", user.getId(), user.getRole());

    String content = String.format(ROLE_CHANGED_CONTENT, user.getRole(), event.changedRole());
    NotificationDto notificationDto = notificationService.create(user, ROLE_CHANGED, content, NotificationLevel.INFO);

    sseService.sendNotification(notificationDto);
    log.info("사용자 권한 변경 이벤트 완료 - notificationId={}", notificationDto.id());
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleNewClothAttributeEvent(NewClothAttributeEvent event) {
    Set<User> receivers = userRepository.findByLockedFalseAndDeleteAtIsNull();
    log.info("의상 속성 추가 이벤트 호출 - ClothingAttributeId={}, Receiver Size={}",
        event.clothAttributeId(), receivers.size());

    String content = String.format(NEW_CLOTH_ATTRIBUTE_CONTENT, event.attributeName());
    List<NotificationDto> notificationDtoList =
        notificationService.createAll(receivers, NEW_CLOTH_ATTRIBUTE, content, NotificationLevel.INFO);

    for(NotificationDto notificationDto : notificationDtoList){
      sseService.sendNotification(notificationDto);
    }
    log.info("의상 속성 추가 이벤트 완료 - notification Size={}", notificationDtoList.size());
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleClothAttributeChangedEvent(ClothAttributeChangedEvent event) {
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

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleFeedLikeEvent(FeedLikedEvent event) {
    log.info("피드 좋아요 이벤트 호출 - feedId={}, likedByUsername={}", event.feedId(), event.likedByUsername());

    String title = String.format(FEED_LIKED, event.likedByUsername());
    NotificationDto notificationDto =
        notificationService.create(event.receiver(), title, event.feedContent(), NotificationLevel.INFO);

    sseService.sendNotification(notificationDto);
    log.info("피드 좋아요 이벤트 완료 - notificationId={}", notificationDto.id());
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleCommentEvent(FeedCommentEvent event) {
    log.info("피드 댓글 이벤트 호출 - feedId={}, commentAuthorUsername={}", event.feedId(), event.commentAuthorUsername());

    String title = String.format(NEW_COMMENT, event.commentAuthorUsername());
    NotificationDto notificationDto =
        notificationService.create(event.feedAuthor(), title, event.commentContent(), NotificationLevel.INFO);

    sseService.sendNotification(notificationDto);
    log.info("피드 댓글 이벤트 완료 - notificationId={}", notificationDto.id());
  }

}

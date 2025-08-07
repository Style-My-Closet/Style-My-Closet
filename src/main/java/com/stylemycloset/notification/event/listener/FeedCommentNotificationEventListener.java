package com.stylemycloset.notification.event.listener;

import com.stylemycloset.notification.dto.NotificationDto;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.notification.event.domain.FeedCommentEvent;
import com.stylemycloset.notification.service.NotificationService;
import com.stylemycloset.sse.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class FeedCommentNotificationEventListener {

  private final NotificationService notificationService;
  private final SseService sseService;

  private static final String NEW_COMMENT = "%s님이 댓글을 달았어요.";

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handler(FeedCommentEvent event) {
    log.info("피드 댓글 이벤트 호출 - feedId={}, commentAuthorUsername={}", event.feedId(), event.commentAuthorUsername());

    String title = String.format(NEW_COMMENT, event.commentAuthorUsername());
    NotificationDto notificationDto =
        notificationService.create(event.feedAuthor(), title, event.commentContent(), NotificationLevel.INFO);

    sseService.sendNotification(notificationDto);
    log.info("피드 댓글 이벤트 완료 - notificationId={}", notificationDto.id());
  }
}

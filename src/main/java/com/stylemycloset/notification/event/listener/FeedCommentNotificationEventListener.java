package com.stylemycloset.notification.event.listener;

import com.stylemycloset.notification.dto.NotificationDto;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.notification.event.NotificationStreamPublisher;
import com.stylemycloset.notification.event.domain.FeedCommentEvent;
import com.stylemycloset.notification.service.NotificationService;
import com.stylemycloset.ootd.entity.Feed;
import com.stylemycloset.ootd.exception.FeedNotFoundException;
import com.stylemycloset.ootd.repo.FeedRepository;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.exception.UserNotFoundException;
import com.stylemycloset.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class FeedCommentNotificationEventListener {

  private final NotificationService notificationService;
  private final FeedRepository feedRepository;
  private final UserRepository userRepository;
  private final NotificationStreamPublisher streamPublisher;

  private static final String NEW_COMMENT = "%s님이 댓글을 달았어요.";

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handler(FeedCommentEvent event) {
    log.info("피드 댓글 이벤트 호출 - feedId={}, feedCommentAuthorId={}", event.feedId(),
        event.feedCommentAuthorId());
    Feed feed = feedRepository.findWithUserById(event.feedId())
        .orElseThrow(() -> new FeedNotFoundException(event.feedId()));
    User feedCommentAuthor = userRepository.findById(event.feedCommentAuthorId())
        .orElseThrow(UserNotFoundException::new);
    try {
      String title = String.format(NEW_COMMENT, feedCommentAuthor.getName());
      NotificationDto dto =
          notificationService.create(feed.getAuthor().getId(), title, feed.getContent(), NotificationLevel.INFO);

      streamPublisher.processAndPublish(List.of(dto));
    } catch (Exception e) {
      log.error("피드 댓글 이벤트 처리 중 예외 발생 - feedId={}, feedCommentAuthorId={}", event.feedId(),
          event.feedCommentAuthorId(), e);
    }
  }
}

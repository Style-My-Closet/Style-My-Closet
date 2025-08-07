package com.stylemycloset.notification.event.listener;

import com.stylemycloset.notification.dto.NotificationDto;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.notification.event.domain.FeedLikedEvent;
import com.stylemycloset.notification.service.NotificationService;
import com.stylemycloset.ootd.entity.Feed;
import com.stylemycloset.ootd.exception.FeedNotFoundException;
import com.stylemycloset.ootd.repo.FeedRepository;
import com.stylemycloset.sse.service.SseService;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.exception.UserNotFoundException;
import com.stylemycloset.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class FeedLikedNotificationEventListener {

  private final NotificationService notificationService;
  private final SseService sseService;
  private final UserRepository userRepository;
  private final FeedRepository feedRepository;

  private static final String FEED_LIKED = "%s님이 내 피드를 좋아합니다.";

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handler(FeedLikedEvent event) {
    try {
      log.info("피드 좋아요 이벤트 호출 - feedId={}, likeUserId={}", event.feedId(), event.likeUserId());
      Feed feed = feedRepository.findWithUserById(event.feedId())
          .orElseThrow(() -> new FeedNotFoundException(event.feedId()));
      User likeUser = userRepository.findById(event.likeUserId())
          .orElseThrow(UserNotFoundException::new);

      String title = String.format(FEED_LIKED, likeUser.getName());
      NotificationDto notificationDto =
          notificationService.create(feed.getAuthor(), title, feed.getContent(), NotificationLevel.INFO);

      sseService.sendNotification(notificationDto);
      log.info("피드 좋아요 이벤트 완료 - notificationId={}", notificationDto.id());
    } catch (Exception e) {
      log.error("피드 좋아요 이벤트 처리 중 예외 발생 - feedId={}, likeUserId={}", event.feedId(), event.likeUserId(), e);
    }
  }
}

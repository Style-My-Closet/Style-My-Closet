package com.stylemycloset.notification.event.listener;

import com.stylemycloset.follow.entity.repository.FollowRepository;
import com.stylemycloset.notification.dto.NotificationDto;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.notification.event.domain.NewFeedEvent;
import com.stylemycloset.notification.service.NotificationService;
import com.stylemycloset.sse.service.SseService;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.exception.UserNotFoundException;
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
public class NewFeedNotificationEventListener {

  private final NotificationService notificationService;
  private final SseService sseService;
  private final UserRepository userRepository;
  private final FollowRepository followRepository;

  private static final String FEED_ADDED = "%s님이 새로운 피드를 작성했어요.";

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handler(NewFeedEvent event) {
    try{
      log.info("새로운 피드 생성 추가 이벤트 호출 - feedId={}, feedAuthorId={}", event.feedId(), event.feedAuthorId());
      User feedAuthor = userRepository.findById(event.feedAuthorId())
          .orElseThrow(UserNotFoundException::new);
      Set<User> receivers = followRepository.findFollowersByFolloweeId(feedAuthor.getId());

      String title = String.format(FEED_ADDED, feedAuthor.getName());

      List<NotificationDto> notificationDtoList =
          notificationService.createAll(receivers, title, event.feedContent(), NotificationLevel.INFO);

      for (NotificationDto notificationDto : notificationDtoList) {
        sseService.sendNotification(notificationDto);
      }
      log.info("새로운 피드 생성 추가 이벤트 완료 - notification Size={}", notificationDtoList.size());
    } catch(Exception e){
      log.info("새로운 피드 생성 추가 이벤트 처리 중 예외 발생 - feedId={}, feedAuthorId={}", event.feedId(), event.feedAuthorId(), e);
    }
  }
}

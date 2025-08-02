package com.stylemycloset.notification.service.impl;

import com.stylemycloset.notification.dto.NotificationDto;
import com.stylemycloset.notification.dto.NotificationDtoCursorResponse;
import com.stylemycloset.notification.dto.NotificationFindAllRequest;
import com.stylemycloset.notification.entity.Notification;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.notification.repository.NotificationQueryRepository;
import com.stylemycloset.notification.repository.NotificationRepository;
import com.stylemycloset.notification.service.NotificationService;
import com.stylemycloset.user.entity.User;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationQueryRepository notificationQueryRepository;

  @Override
  @Transactional
  public NotificationDto create(User receiver, String title, String content, NotificationLevel level) {
    log.info("단일 알림 생성 시작: receiver={}, title={},content={},level={}",  receiver, title, content, level);

    Notification notification = new Notification(receiver, title, content, level);
    notificationRepository.save(notification);

    log.info("단일 알림 생성 완료 : {}", notification);
    return NotificationDto.from(notification);
  }

  @Override
  @Transactional
  public List<NotificationDto> createAll(Set<User> receivers, String title, String content, NotificationLevel level) {
    log.info("여러 알림 생성 시작: 수신자 수={}, title={},content={},level={}", receivers.size(), title, content, level);

    List<Notification> notifications = receivers.stream()
            .map(receiver -> new Notification(receiver, title, content, level))
            .toList();

    notificationRepository.saveAll(notifications);

    return NotificationDto.fromList(notifications);
  }

  @Override
  @Transactional
  // @PreAuthorize("principal.userDto.id == #receiverId")
  public void delete(long receiverId, long notificationId) {
    Notification notification = notificationRepository.findById(notificationId).orElse(null);

    if(notification == null) {
      log.info("이미 삭제된 알림이거나 존재하지 않은 알림: notificationId={}", notificationId);
      return;
    }

    notificationRepository.delete(notification);
  }

  @Override
  @Transactional(readOnly = true)
  // @PreAuthorize("principal.userDto.id == #userId")
  public NotificationDtoCursorResponse findAll(long userId, NotificationFindAllRequest request) {
    List<Notification> notifications = notificationQueryRepository.findAllByCursor(request, userId);
    long totalCount = notificationRepository.countByReceiverId(userId);

    boolean hasNext = !notifications.isEmpty() && notifications.size() > request.limit();

    int size = hasNext ? request.limit() : notifications.size();
    List<NotificationDto> data = notifications.stream()
        .limit(size)
        .map(NotificationDto::from)
        .toList();

    if(hasNext) {
      NotificationDto lastDto = data.getLast();

      return NotificationDtoCursorResponse.of(
          data,
          lastDto.createdAt(),
          lastDto.id(),
          hasNext,
          totalCount
      );
    }
    return NotificationDtoCursorResponse.of(
        data,
        null,
        0L,
        hasNext,
        totalCount
    );
  }
}

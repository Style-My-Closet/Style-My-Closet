package com.stylemycloset.notification.service;

import com.stylemycloset.notification.dto.NotificationDto;
import com.stylemycloset.notification.dto.NotificationDtoCursorResponse;
import com.stylemycloset.notification.dto.NotificationFindAllRequest;
import com.stylemycloset.notification.entity.NotificationLevel;
import java.util.List;
import java.util.Set;

public interface NotificationService {

  NotificationDto create(Long receiverId, String title, String content, NotificationLevel level);

  List<NotificationDto> createAll(Set<Long> receiverIds, String title, String content, NotificationLevel level);

  void delete(long receiverId, long notificationId);

  NotificationDtoCursorResponse findAllByCursor(long userId, NotificationFindAllRequest request);
}

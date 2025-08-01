package com.stylemycloset.notification.service;

import com.stylemycloset.notification.dto.NotificationDtoCursorResponse;
import com.stylemycloset.notification.dto.NotificationFindAllRequest;

public interface NotificationService {

  void delete(long receiverId, long notificationId);

  NotificationDtoCursorResponse findAll(long userId, NotificationFindAllRequest request);
}

package com.stylemycloset.notification.service;

import com.stylemycloset.notification.dto.NotificationDto;
import com.stylemycloset.notification.dto.NotificationDtoCursorResponse;
import com.stylemycloset.notification.dto.NotificationFindAllRequest;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.user.entity.User;
import java.util.List;
import java.util.Set;

public interface NotificationService {

  NotificationDto create(User receiver, String title, String content, NotificationLevel level);

  List<NotificationDto> createAll(Set<User> receivers, String title, String content, NotificationLevel level);

  void delete(long receiverId, long notificationId);

  NotificationDtoCursorResponse findAll(long userId, NotificationFindAllRequest request);
}

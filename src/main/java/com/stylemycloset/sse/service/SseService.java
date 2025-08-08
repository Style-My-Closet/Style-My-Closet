package com.stylemycloset.sse.service;

import com.stylemycloset.notification.dto.NotificationDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseService {

  SseEmitter connect(Long userId, String eventId, String lastEventId);

  void sendNotification(NotificationDto notificationDto);

  void cleanUpSseEmitter();

  void cleanUpSseInfos();
}

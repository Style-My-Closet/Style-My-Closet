package com.stylemycloset.sse.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseService {

  SseEmitter connect(Long userId, String eventId, String lastEventId);

  void sendWeatherAlert(Long weatherId, String message);
}

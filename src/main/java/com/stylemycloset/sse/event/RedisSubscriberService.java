package com.stylemycloset.sse.event;

import com.stylemycloset.sse.cache.SseNotificationInfoCache;
import com.stylemycloset.sse.dto.NotificationDtoWithId;
import com.stylemycloset.sse.repository.SseRepository;
import com.stylemycloset.sse.service.SseSender;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSubscriberService {

  private final SseRepository sseRepository;
  private final SseNotificationInfoCache cache;
  private final SseSender sseSender;

  private final ConcurrentHashMap<Long, Boolean> inflight = new ConcurrentHashMap<>();
  private static final String EVENT_NAME = "notifications";

  @Async("streamMessageExecutor")
  public void deliver(Long userId) {

    if(inflight.putIfAbsent(userId, true) != null) return;

    try{
      Deque<SseEmitter> emitters = sseRepository.findOrCreateEmitters(userId);
      if(emitters == null || emitters.isEmpty()) return;

      String lastDeliveredId = cache.getLastDeliveredId(userId);
      if(lastDeliveredId == null) lastDeliveredId = "0-0";

      while (true) {
        List<NotificationDtoWithId> eventData = cache.getNotificationInfo(userId, lastDeliveredId);
        if(eventData == null || eventData.isEmpty()) break;

        for(SseEmitter emitter : emitters) {
          for(NotificationDtoWithId data : eventData) {
            sseSender.sendToClient(userId, emitter, data.eventId(), EVENT_NAME, data.dto());
          }
        }
        lastDeliveredId = eventData.getLast().eventId();
        cache.addLastDeliveredId(userId, lastDeliveredId);
      }
    } finally {
      inflight.remove(userId);
    }
  }
}

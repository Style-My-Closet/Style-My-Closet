package com.stylemycloset.sse.service.impl;

import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import com.stylemycloset.sse.dto.SseInfo;
import com.stylemycloset.sse.repository.SseRepository;
import com.stylemycloset.sse.service.SseService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@Slf4j
@RequiredArgsConstructor
public class SseServiceImpl implements SseService {

  private final SseRepository sseRepository;

  private final ConcurrentHashMap<Long, List<SseInfo>> userEvents = new ConcurrentHashMap<>();
  private static final long DEFAULT_TIMEOUT = 30L * 60 * 1000;
  private final AtomicLong eventIdSequence = new AtomicLong();

  public SseEmitter connect(Long userId, String eventId, String lastEventId) {

    SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
    sseRepository.save(userId, emitter);

    emitter.onCompletion(() -> sseRepository.delete(userId, emitter));
    emitter.onTimeout(() -> sseRepository.delete(userId, emitter));
    emitter.onError(e -> sseRepository.delete(userId, emitter));

    sendToClient(userId, emitter, eventId, "connect", "Sse Connected");

    if(lastEventId != null &&  !lastEventId.isEmpty()) {
      try {
        long lastId = Long.parseLong(lastEventId);
        List<SseInfo> missedInfo = userEvents.getOrDefault(userId, new CopyOnWriteArrayList<>())
            .stream()
            .filter(event ->
                event.id() > lastId
            ).toList();
        log.debug("missedInfo Size={}", missedInfo.size());

        for (SseInfo info : missedInfo) {
          sendToClient(userId, emitter, String.valueOf(info.id()), info.name(), info.data());
        }
      } catch(NumberFormatException e) {
        log.warn("유효하지 않는 lastEventId 형식 : {}", lastEventId);
      }
    }

    return emitter;
  }

  private void sendToClient(Long userId, SseEmitter emitter, String eventId, String eventName, Object data) {
    try{
      emitter.send(SseEmitter.event()
          .id(eventId)
          .name(eventName)
          .data(data));
      log.debug("[{}]의 {} SSE 이벤트 수신 완료 (eventId: {})", userId, eventName, eventId);
    } catch (IOException e){
      log.warn("[{}]의 {} SSE 이벤트 실패 (eventId: {})", userId, eventName, eventId, e);
      sseRepository.delete(userId, emitter);
    }
  }

  @Scheduled(fixedRate = 2 * 60 * 1000)
  private void cleanUpSseEmitter() {
    sseRepository.getAllEmittersReadOnly()
        .forEach((userId, emitters) ->
            emitters.forEach(emitter -> {
              try{
                emitter.send(SseEmitter.event().name("heartbeat").data("data"));
              } catch (IOException e){
                log.debug("user [{}]에 대한 연결이 실패하여 emitter를 삭제", userId);
                sseRepository.delete(userId, emitter);
              }
            }));
  }

  @Scheduled(fixedRate = 60 * 60 * 1000)
  private void cleanUpSseInfos() {
    long timeout = System.currentTimeMillis() - (60 * 60 * 1000);
    userEvents.forEach((userId, events) -> {
      events.removeIf(event -> event.createdAt() < timeout);
      if(events.isEmpty()) userEvents.remove(userId);
    });
  }


  @Override
  public void sendWeatherAlert(Long weatherId, String message) {
    List<SseEmitter> emitters = sseRepository.get(weatherId);
    if (emitters == null || emitters.isEmpty()) return;

    long eventId = eventIdSequence.incrementAndGet();

    for (SseEmitter emitter : emitters) {
      Long userId = sseRepository.getUserIdByEmitter(emitter).orElseThrow(
          ()->new StyleMyClosetException(ErrorCode.ERROR_CODE, Map.of("userId","userId"))
      );

      SseInfo info = new SseInfo(eventId, "weather-alert", message, System.currentTimeMillis());
      userEvents.computeIfAbsent(userId, id -> new CopyOnWriteArrayList<>()).add(info);

      sendToClient(userId, emitter, String.valueOf(eventId), info.name(), info.data());
    }
  }

}

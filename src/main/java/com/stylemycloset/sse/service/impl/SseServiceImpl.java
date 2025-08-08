package com.stylemycloset.sse.service.impl;

import com.stylemycloset.notification.dto.NotificationDto;
import com.stylemycloset.sse.dto.SseInfo;
import com.stylemycloset.sse.exception.SseSendFailureException;
import com.stylemycloset.sse.repository.SseRepository;
import com.stylemycloset.sse.service.SseSender;
import com.stylemycloset.sse.service.SseService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@Slf4j
@RequiredArgsConstructor
public class SseServiceImpl implements SseService {

  private final SseRepository sseRepository;
  private final SseSender sseSender;

  private final ConcurrentHashMap<Long, List<SseInfo>> userEvents = new ConcurrentHashMap<>();
  private static final long DEFAULT_TIMEOUT = 30L * 60 * 1000;
  private static final int MAX_EVENT_COUNT = 30;
  private static final int MAX_EMITTER_COUNT = 3;
  private static final String EVENT_NAME = "notifications";

  @Override
  public SseEmitter connect(Long userId, String eventId, String lastEventId) {
    SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

    CopyOnWriteArrayList<SseEmitter> emitters = sseRepository.findByUserId(userId);

    if(emitters.size() >= MAX_EMITTER_COUNT) {
      SseEmitter removed = emitters.removeFirst();
      removed.complete();
      log.info("최대 emitter 수를 초과하여 오래된 emitter 제거됨 : userId={}, size={}", userId, emitters.size());
    }
    emitters.add(emitter);
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

  @Retryable(
      retryFor = SseSendFailureException.class,
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2)
  )
  private void sendToClient(Long userId, SseEmitter emitter, String eventId, String eventName, Object data) {
    try{
      emitter.send(SseEmitter.event()
          .id(eventId)
          .name(eventName)
          .data(data));
      log.debug("[{}]의 {} 동기 SSE 이벤트 수신 완료 (eventId: {})", userId, eventName, eventId);
    } catch (IOException e){
      throw new SseSendFailureException(userId, eventId);
    }
  }

  @Recover
  public void recover(SseSendFailureException e, Long userId, SseEmitter emitter, String eventId, String eventName, Object data) {
    log.warn("[{}]의 {} SSE 이벤트 재시도 모두 실패 (eventId: {})", userId, eventName, eventId, e);
    sseRepository.delete(userId, emitter);
  }

  @Override
  @Async("sseTaskExecutor")
  public void sendNotification(NotificationDto notificationDto) {
    Long receiverId = notificationDto.receiverId();
    List<SseEmitter> sseEmitters = sseRepository.findByUserId(receiverId);
    if(sseEmitters.isEmpty()) {
      log.info("SSE 연결이 없어 알림 전송 실패 : userId={}, notificationId={}",
          receiverId, notificationDto.id());
      return;
    }

    long eventId = notificationDto.createdAt().toEpochMilli();
    SseInfo sseInfo = new SseInfo(eventId, EVENT_NAME, notificationDto, System.currentTimeMillis());

    List<SseInfo> eventList = userEvents.computeIfAbsent(receiverId, k -> new CopyOnWriteArrayList<>());
    synchronized (eventList) {
      if (eventList.size() >= MAX_EVENT_COUNT) {
        eventList.removeFirst();
      }
      eventList.add(sseInfo);
    }

    for(SseEmitter sseEmitter : sseEmitters) {
      sseSender.sendToClientAsync(receiverId, sseEmitter, String.valueOf(eventId), EVENT_NAME, notificationDto);
    }
  }

  @Scheduled(fixedRate = 2 * 60 * 1000)
  public void cleanUpSseEmitter() {
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
  public void cleanUpSseInfos() {
    long timeout = System.currentTimeMillis() - (60 * 60 * 1000);
    userEvents.forEach((userId, events) -> {
      events.removeIf(event -> event.createdAt() < timeout);
      if(events.isEmpty()) userEvents.remove(userId);
    });
  }

}

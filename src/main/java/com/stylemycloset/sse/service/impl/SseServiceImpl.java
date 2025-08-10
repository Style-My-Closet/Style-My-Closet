package com.stylemycloset.sse.service.impl;

import com.stylemycloset.notification.dto.NotificationDto;
import com.stylemycloset.sse.dto.SseInfo;
import com.stylemycloset.sse.repository.SseRepository;
import com.stylemycloset.sse.service.SseSender;
import com.stylemycloset.sse.service.SseService;
import java.io.IOException;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
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
  private final SseSender sseSender;

  private static final long DEFAULT_TIMEOUT = 30L * 60 * 1000;
  private static final int MAX_EVENT_COUNT = 30;
  private static final int MAX_EMITTER_COUNT = 3;
  private static final String EVENT_NAME = "notifications";

  @Override
  public SseEmitter connect(Long userId, String eventId, String lastEventId) {
    SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

    CopyOnWriteArrayList<SseEmitter> emitters = sseRepository.findOrCreateEmitters(userId);

    emitter.onCompletion(() -> sseRepository.removeEmitter(userId, emitter));
    emitter.onTimeout(() -> sseRepository.removeEmitter(userId, emitter));
    emitter.onError(e -> sseRepository.removeEmitter(userId, emitter));

    if(emitters.size() >= MAX_EMITTER_COUNT) {
      SseEmitter removed = emitters.remove(0);
      removed.complete();
      log.info("최대 emitter 수를 초과하여 오래된 emitter 제거됨 : userId={}, size={}", userId, emitters.size());
    }
    sseRepository.addEmitter(userId, emitter);


    sseSender.sendToClient(userId, emitter, eventId, "connect", "Sse Connected");

    if(lastEventId != null &&  !lastEventId.isEmpty()) {
      try {
        long lastId = Long.parseLong(lastEventId);
        List<SseInfo> missedInfo = sseRepository.findOrCreateEvents(userId)
            .stream()
            .filter(event ->
                event.id() > lastId
            ).toList();
        log.debug("missedInfo Size={}", missedInfo.size());

        for (SseInfo info : missedInfo) {
          sseSender.sendToClient(userId, emitter, String.valueOf(info.id()), info.name(), info.data());
        }
      } catch(NumberFormatException e) {
        log.warn("유효하지 않는 lastEventId 형식 : {}", lastEventId);
      }
    }

    return emitter;
  }

  @Override
  public void sendNotification(NotificationDto notificationDto) {
    Long receiverId = notificationDto.receiverId();
    List<SseEmitter> sseEmitters = sseRepository.findOrCreateEmitters(receiverId);
    if(sseEmitters.isEmpty()) {
      log.info("SSE 연결이 없어 알림 전송 실패 : userId={}, notificationId={}",
          receiverId, notificationDto.id());
      return;
    }

    long eventId = notificationDto.createdAt().toEpochMilli();
    SseInfo sseInfo = new SseInfo(eventId, EVENT_NAME, notificationDto, System.currentTimeMillis());
    Deque<SseInfo> eventDeque = sseRepository.findOrCreateEvents(receiverId);

    if (eventDeque.size() >= MAX_EVENT_COUNT) {
      eventDeque.pollFirst();
    }
    eventDeque.add(sseInfo);

    for(SseEmitter sseEmitter : sseEmitters) {
      sseSender.sendToClientAsync(receiverId, sseEmitter, String.valueOf(eventId), EVENT_NAME, notificationDto);
    }
  }

  @Scheduled(fixedRate = 2 * 60 * 1000)
  public void cleanUpSseEmitters() {
    sseRepository.findAllEmittersReadOnly()
        .forEach((userId, emitters) ->
            emitters.forEach(emitter -> {
              try{
                emitter.send(SseEmitter.event().name("heartbeat").data("data"));
              } catch (IOException | IllegalStateException e){
                log.debug("user [{}]에 대한 연결이 실패하여 emitter를 삭제", userId);
                sseRepository.removeEmitter(userId, emitter);
              }
            }));
  }

  @Scheduled(fixedRate = 60 * 60 * 1000)
  public void cleanUpSseInfos() {
    long timeout = System.currentTimeMillis() - (60 * 60 * 1000);
    sseRepository.cleanEventOlderThan(timeout);
  }

}

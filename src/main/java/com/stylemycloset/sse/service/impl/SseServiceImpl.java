package com.stylemycloset.sse.service.impl;

import com.stylemycloset.sse.dto.SseInfo;
import com.stylemycloset.sse.repository.SseRepository;
import com.stylemycloset.sse.service.SseService;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
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

  private static final long DEFAULT_TIMEOUT = 30L * 60 * 1000;
  private final SseRepository sseRepository;
  private final ConcurrentHashMap<Long, List<SseInfo>> userEvents = new ConcurrentHashMap<>();

  /**
   * 지정된 사용자에 대해 SSE(Server-Sent Events) 연결을 생성하고, 초기 연결 이벤트 및 누락된 이벤트를 클라이언트로 전송합니다.
   *
   * @param userId SSE 연결을 생성할 사용자 ID
   * @param eventId 초기 연결 이벤트의 식별자
   * @param lastEventId 클라이언트가 마지막으로 수신한 이벤트 ID(누락된 이벤트 복구에 사용, null 또는 빈 문자열일 수 있음)
   * @return 생성된 SseEmitter 인스턴스
   *
   * lastEventId가 유효한 경우, 해당 ID 이후의 누락된 이벤트를 모두 클라이언트로 전송합니다. lastEventId가 잘못된 형식이면 경고 로그를 남기고 무시합니다.
   */
  public SseEmitter connect(Long userId, String eventId, String lastEventId) {

    SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
    sseRepository.save(userId, emitter);

    emitter.onCompletion(() -> sseRepository.delete(userId, emitter));
    emitter.onTimeout(() -> sseRepository.delete(userId, emitter));
    emitter.onError(e -> sseRepository.delete(userId, emitter));

    sendToClient(userId, emitter, eventId, "connect", "Sse Connected");

    if (lastEventId != null && !lastEventId.isEmpty()) {
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
      } catch (NumberFormatException e) {
        log.warn("유효하지 않는 lastEventId 형식 : {}", lastEventId);
      }
    }

    return emitter;
  }

  /**
   * 지정된 사용자에게 SSE 이벤트를 전송합니다.
   *
   * @param userId    이벤트를 받을 사용자 ID
   * @param emitter   이벤트를 전송할 SseEmitter 인스턴스
   * @param eventId   이벤트의 고유 식별자
   * @param eventName 이벤트 이름
   * @param data      전송할 데이터 객체
   */
  private void sendToClient(Long userId, SseEmitter emitter, String eventId, String eventName,
      Object data) {
    try {
      emitter.send(SseEmitter.event()
          .id(eventId)
          .name(eventName)
          .data(data));
      log.debug("[{}]의 {} SSE 이벤트 수신 완료 (eventId: {})", userId, eventName, eventId);
    } catch (IOException e) {
      log.warn("[{}]의 {} SSE 이벤트 실패 (eventId: {})", userId, eventName, eventId, e);
      sseRepository.delete(userId, emitter);
    }
  }

  /**
   * 모든 SSE Emitter에 주기적으로 하트비트 이벤트를 전송하여 연결을 유지하고, 전송 실패 시 해당 Emitter를 삭제합니다.
   *
   * 2분마다 실행됩니다.
   */
  @Scheduled(fixedRate = 2 * 60 * 1000)
  private void cleanUpSseEmitter() {
    sseRepository.getAllEmittersReadOnly()
        .forEach((userId, emitters) ->
            emitters.forEach(emitter -> {
              try {
                emitter.send(SseEmitter.event().name("heartbeat").data("data"));
              } catch (IOException e) {
                log.debug("user [{}]에 대한 연결이 실패하여 emitter를 삭제", userId);
                sseRepository.delete(userId, emitter);
              }
            }));
  }

  /**
   * 한 시간 이상 지난 SSE 이벤트 정보를 정기적으로 삭제하여 메모리 사용을 최적화합니다.
   *
   * 이 메서드는 매 시간마다 실행되며, 각 사용자별로 저장된 이벤트 중 생성된 지 한 시간 이상 경과한 항목을 제거합니다.
   * 모든 이벤트가 삭제된 사용자는 `userEvents` 맵에서 해당 엔트리가 제거됩니다.
   */
  @Scheduled(fixedRate = 60 * 60 * 1000)
  private void cleanUpSseInfos() {
    long timeout = System.currentTimeMillis() - (60 * 60 * 1000);
    userEvents.forEach((userId, events) -> {
      events.removeIf(event -> event.createdAt() < timeout);
      if (events.isEmpty()) {
        userEvents.remove(userId);
      }
    });
  }

}

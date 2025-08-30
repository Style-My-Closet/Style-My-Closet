package com.stylemycloset.sse.service.impl;

import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import com.stylemycloset.sse.cache.SseNotificationInfoCache;
import com.stylemycloset.sse.dto.NotificationDtoWithId;
import com.stylemycloset.sse.repository.SseRepository;
import com.stylemycloset.sse.service.SseSender;
import com.stylemycloset.sse.service.SseService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
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
  private final SseNotificationInfoCache cache;

  private static final long DEFAULT_TIMEOUT = 5L * 60 * 1000;
  private static final String EVENT_NAME = "notifications";

  @Override
  public SseEmitter connect(Long userId, String eventId, String lastEventId) {
    SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

    emitter.onCompletion(() -> sseRepository.removeEmitter(userId, emitter));
    emitter.onTimeout(() -> {
      try { emitter.complete(); } catch (RuntimeException ignore) {}
      sseRepository.removeEmitter(userId, emitter);
    });
    emitter.onError(e -> {
      try { emitter.complete(); } catch (RuntimeException ignore) {}
      sseRepository.removeEmitter(userId, emitter);
    });

    SseEmitter computeEmitter = sseRepository.addEmitter(userId, emitter);
    if(computeEmitter != null) {
      computeEmitter.complete();
    }

    if (lastEventId != null && !lastEventId.isEmpty() && !isValidStreamsId(lastEventId)) {
      log.info("유효하지 않은 LastEventId로 요청. userId={}, lastEventId={}", userId, lastEventId);
      throw new StyleMyClosetException(ErrorCode.INVALID_INPUT_VALUE, Map.of("lastEventId", lastEventId));
    }

    sseSender.sendToClient(userId, emitter, eventId, "connect", "Sse Connected");

    if(lastEventId != null &&  !lastEventId.isEmpty()) {
      List<NotificationDtoWithId> missedInfo = cache.getNotificationInfo(userId, lastEventId);
      log.debug("놓친 알림 이벤트 개수={}", missedInfo.size());

      for (NotificationDtoWithId info : missedInfo) {
        sseSender.sendToClient(userId, emitter, info.eventId(), EVENT_NAME, info.dto());
      }
    }

    return emitter;
  }

  public boolean isValidStreamsId(String lastEventId) {
    return Pattern.compile("^\\d+-\\d+$").matcher(lastEventId).matches();
  }

  @Scheduled(fixedRate = 30 * 1000)
  public void cleanUpSseEmitters() {
    sseRepository.findAllEmittersReadOnly()
        .forEach((userId, emitters) ->
            emitters.forEach(emitter -> {
              try{
                emitter.send(SseEmitter.event().name("heartbeat").data("ping"));
              } catch (IOException | IllegalStateException e){
                log.debug("user [{}]에 대한 연결이 실패하여 emitter를 삭제", userId);
                try { emitter.complete(); } catch (RuntimeException ignore) {}
                sseRepository.removeEmitter(userId, emitter);
              }
            }));
  }

  @Scheduled(fixedDelay = 5 * 60 * 1000)
  public void cleanNotificationInfos() {
    cache.trimNotificationInfos();
  }

}

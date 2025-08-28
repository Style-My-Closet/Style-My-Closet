package com.stylemycloset.sse.service;

import com.stylemycloset.sse.repository.SseRepository;
import java.io.IOException;
import java.io.UncheckedIOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@Slf4j
@RequiredArgsConstructor
public class SseSender {
  private final SseRepository sseRepository;

  @Retryable(
      retryFor = UncheckedIOException.class,
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2)
  )
  public void sendToClient(Long userId, SseEmitter emitter, String eventId, String eventName, Object data) {
    try {
      emitter.send(SseEmitter.event()
          .id(eventId)
          .name(eventName)
          .data(data));
      log.debug("[{}] {} SSE 이벤트 전송 성공 (eventId: {})", userId, eventName, eventId);
    } catch (IllegalStateException e) {
      log.warn("[{}] {} 전송 실패 - emitter가 이미 완료됨. 즉시 제거 (eventId: {})", userId, eventName, eventId, e);
      try { emitter.complete(); } catch (RuntimeException ignore) {}
      sseRepository.removeEmitter(userId, emitter);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Recover
  public void recover(UncheckedIOException e, Long userId, SseEmitter emitter,
      String eventId, String eventName, Object data) {
    log.warn("[{}] {} 재시도 모두 실패 (eventId: {})", userId, eventName, eventId, e);
    try { emitter.complete(); } catch (RuntimeException ignore) {}
    sseRepository.removeEmitter(userId, emitter);
  }
}

package com.stylemycloset.sse.service;

import com.stylemycloset.sse.exception.SseSendFailureException;
import com.stylemycloset.sse.repository.SseRepository;
import java.io.IOException;
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

  @Async("sseTaskExecutor")
  @Retryable(
      retryFor = SseSendFailureException.class,
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2)
  )
  public void sendToClientAsync(Long userId, SseEmitter emitter, String eventId, String eventName, Object data) {
    try{
      emitter.send(SseEmitter.event()
          .id(eventId)
          .name(eventName)
          .data(data));
      log.debug("[{}]의 {} 비동기 SSE 이벤트 수신 완료 (eventId: {})", userId, eventName, eventId);
    } catch (IOException e){
      throw new SseSendFailureException(userId, eventId);
    }
  }

  @Recover
  public void recover(SseSendFailureException  e, Long userId, SseEmitter emitter, String eventId, String eventName, Object data) {
    log.warn("[{}]의 {} SSE 이벤트 재시도 모두 실패 (eventId: {})", userId, eventName, eventId, e);
    sseRepository.delete(userId, emitter);
  }
}

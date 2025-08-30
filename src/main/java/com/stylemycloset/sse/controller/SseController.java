package com.stylemycloset.sse.controller;

import com.stylemycloset.sse.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
public class SseController {

  private final SseService sseService;

  @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter connect(
      @AuthenticationPrincipal(expression = "userId") Long userId,
      @RequestParam(value = "LastEventId", required = false) String lastEventId
  ) {
    log.info("SSE 연결 요청. LastEventId: {}", lastEventId);
    String eventId = System.currentTimeMillis() + "-0";
    return sseService.connect(userId, eventId, lastEventId);
  }
}

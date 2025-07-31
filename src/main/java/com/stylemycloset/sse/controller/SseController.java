package com.stylemycloset.sse.controller;

import com.stylemycloset.sse.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
public class SseController {

  private final SseService sseService;

  @GetMapping(path = "/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter connect(
      // UserDetails 구현체 생성될 시 수정 필요 (UserDetails의 user 관련 정보로 대체)
      @PathVariable Long userId,
      @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId
  ) {
    String eventId = String.valueOf(System.currentTimeMillis());
    return sseService.connect(userId, eventId, lastEventId);
  }
}

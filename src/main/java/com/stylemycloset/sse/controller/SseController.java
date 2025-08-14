package com.stylemycloset.sse.controller;

import com.stylemycloset.sse.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
public class SseController {

  private final SseService sseService;

  @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @PreAuthorize("isAuthenticated()")
  public SseEmitter connect(
      @AuthenticationPrincipal(expression = "userId") Long userId,
      @RequestParam(value = "lastEventId", required = false) String lastEventId
  ) {
    String eventId = String.valueOf(System.currentTimeMillis());
    return sseService.connect(userId, eventId, lastEventId);
  }
}

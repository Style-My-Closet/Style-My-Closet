package com.stylemycloset.sse.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.stylemycloset.sse.repository.SseRepository;
import com.stylemycloset.sse.service.impl.SseServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
public class SseServiceUnitTest {

  @Mock
  SseRepository sseRepository;

  @Mock
  SseSender sseSender;

  @InjectMocks
  SseServiceImpl sseService;

  @DisplayName("lastEventId가 없다면 emitter를 저장한다.")
  @Test
  void connect_addNewEmitter_whenUnderLimit() {
    // given
    Long userId = 1L;
    given(sseRepository.addEmitter(any(Long.class), any(SseEmitter.class))).willReturn(null);

    // when
    SseEmitter res = sseService.connect(userId, String.valueOf(1_726_000_000_000L), null);

    // then
    verify(sseRepository).addEmitter(userId, res);
    verify(sseSender).sendToClient(eq(userId), any(SseEmitter.class), any(String.class), eq("connect"), eq("Sse Connected"));
  }

  @DisplayName("Emitter개수가 MAX_EMITTER_COUNT이상이면 가장 오래된 emitter를 제거한다.")
  @Test
  void connect_shouldRemoveOldestEmitterIfExceedsLimit() {
    // given
    Long userId = 1L;

    SseEmitter emitter = mock(SseEmitter.class);
    given(sseRepository.addEmitter(any(Long.class), any(SseEmitter.class))).willReturn(emitter);

    // when
    sseService.connect(userId, String.valueOf(1_726_000_000_000L), null);

    // then
    verify(emitter).complete();
    verify(sseSender).sendToClient(eq(userId), any(SseEmitter.class), any(String.class), eq("connect"), eq("Sse Connected"));
  }
}

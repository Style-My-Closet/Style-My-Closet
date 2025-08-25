package com.stylemycloset.sse.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.stylemycloset.notification.dto.NotificationDto;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.sse.cache.SseNotificationInfoCache;
import com.stylemycloset.sse.repository.SseRepository;
import com.stylemycloset.sse.service.impl.SseServiceImpl;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.List;
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

  @Mock
  SseNotificationInfoCache cache;

  @DisplayName("lastEventId가 없다면 emitter를 저장한다.")
  @Test
  void connect_addNewEmitter_whenUnderLimit() throws Exception {
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

  @DisplayName("연결된 Emitter가 없으면 캐시에 저장하거나 전송하지 않는다")
  @Test
  void sendNotification_noEmitters_shouldDoNothing() throws Exception {
    // given
    Long userId = 1L;
    NotificationDto dto = new NotificationDto(10L, Instant.now(), userId, "t", "b", NotificationLevel.INFO);
    given(sseRepository.findOrCreateEmitters(userId)).willReturn(new ArrayDeque<>());

    // when
    sseService.sendNotification(dto);

    // then
    verify(cache, never()).addNotificationInfo(anyLong(), any());
    verifyNoInteractions(sseSender);
  }

  @DisplayName("sendNotification() 호출 시 캐시에 저장한다")
  @Test
  void sendNotification_storeToCache_and_sendToAllEmitters() throws Exception {
    // given
    Long userId = 1L;
    NotificationDto dto = new NotificationDto(10L, Instant.now(), userId, "test", "test", NotificationLevel.INFO);

    SseEmitter emitterA = new SseEmitter();
    given(sseRepository.findOrCreateEmitters(userId))
        .willReturn(new ArrayDeque<>(List.of(emitterA)));

    String recordId = "1754037511000-0";
    given(cache.addNotificationInfo(userId, dto)).willReturn(recordId);

    // when
    sseService.sendNotification(dto);

    // then
    verify(cache).addNotificationInfo(userId, dto);
    verify(sseSender).sendToClientAsync(eq(userId), eq(emitterA), eq(recordId), anyString(), eq(dto));
  }
}

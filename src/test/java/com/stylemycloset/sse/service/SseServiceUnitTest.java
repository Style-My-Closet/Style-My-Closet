package com.stylemycloset.sse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.stylemycloset.notification.dto.NotificationDto;
import com.stylemycloset.notification.entity.NotificationLevel;
import com.stylemycloset.sse.dto.SseInfo;
import com.stylemycloset.sse.repository.SseRepository;
import com.stylemycloset.sse.service.impl.SseServiceImpl;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
public class SseServiceUnitTest {

  private static final Logger log = LoggerFactory.getLogger(SseServiceUnitTest.class);
  @Mock
  SseRepository sseRepository;

  @Mock
  SseSender sseSender;

  @InjectMocks
  SseServiceImpl sseService;

  private static final int MAX_EMITTER_COUNT = 3;
  private static final int MAX_EVENT_COUNT = 30;

  @DisplayName("lastEventId가 없다면 emitter를 저장한다.")
  @Test
  void connect_addNewEmitter_whenUnderLimit() throws Exception {
    // given
    Long userId = 111L;
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
    Long userId = 9L;

    SseEmitter emitter = mock(SseEmitter.class);
    given(sseRepository.addEmitter(any(Long.class), any(SseEmitter.class))).willReturn(emitter);

    // when
    sseService.connect(userId, String.valueOf(1_726_000_000_000L), null);

    // then
    verify(emitter).complete();
    verify(sseSender).sendToClient(eq(userId), any(SseEmitter.class), any(String.class), eq("connect"), eq("Sse Connected"));
  }

  @DisplayName("lastEventId와 함께 connect()를 호출하면 놓친 알림을 보낸다")
  @Test
  void connect_withLastEventId_whenUnderLimit_sendNotification() throws Exception {
    // given
    Long userId = 1L;
    long lastEventId = 1L;

    given(sseRepository.addEmitter(any(Long.class), any(SseEmitter.class))).willReturn(null);

    SseInfo sseInfo1 = new SseInfo(2L, "notifications", "데이터1", 2);
    SseInfo sseInfo2 = new SseInfo(3L, "notifications", "데이터2", 3);
    Deque<SseInfo> events = new ArrayDeque<>(List.of(sseInfo1, sseInfo2));
    given(sseRepository.findOrCreateEvents(userId)).willReturn(events);

    // when
    sseService.connect(userId, "1", String.valueOf(lastEventId));

    // then
    verify(sseSender).sendToClient(eq(userId), any(SseEmitter.class), eq("1"),
        eq("connect"), eq("Sse Connected"));
    verify(sseSender).sendToClient(eq(userId), any(SseEmitter.class), eq("2"),
        eq("notifications"), eq("데이터1"));
    verify(sseSender).sendToClient(eq(userId), any(SseEmitter.class), eq("3"),
        eq("notifications"), eq("데이터2"));
  }

  @DisplayName("이벤트 개수가 MAX_EVENT_COUNT 이상이면 가장 오래된 이벤트를 제거한다")
  @Test
  void sendNotification_shouldRemoveOldestEventIfExceedsLimit() {
    // given
    Long userId = 10L;
    NotificationDto newNotification = mock(NotificationDto.class);
    given(newNotification.receiverId()).willReturn(userId);
    given(newNotification.createdAt()).willReturn(Instant.now());

    Deque<SseEmitter> emitters = new ArrayDeque<>();
    emitters.add(mock(SseEmitter.class));
    given(sseRepository.findOrCreateEmitters(userId)).willReturn(emitters);

    Deque<SseInfo> events = new ArrayDeque<>();
    for (int i = 0; i < MAX_EVENT_COUNT; i++) {
      events.add(new SseInfo(i, "notifications", mock(NotificationDto.class), 1L));
    }
    SseInfo oldest = events.getFirst();
    // when
    sseService.sendNotification(newNotification);

    // then
    assertThat(events).hasSize(MAX_EVENT_COUNT);
    verify(sseRepository).addEvent(eq(userId), any(SseInfo.class));
  }

  @DisplayName("sendNotification() 호출 시 userEvents에 이벤트가 저장된다")
  @Test
  void sendNotification_shouldStoreEventInUserEvents() {
    // given
    Long userId = 11L;
    Instant createdAt = Instant.parse("2025-08-09T00:18:43.527Z");
    String eventId = String.valueOf(createdAt.toEpochMilli());

    NotificationDto notificationDto = new NotificationDto(1L, createdAt, userId, "테스트1", "테스트1", NotificationLevel.INFO);

    Deque<SseEmitter> emitters = new ArrayDeque<>();
    emitters.add(mock(SseEmitter.class));
    given(sseRepository.findOrCreateEmitters(userId)).willReturn(emitters);

    // when
    sseService.sendNotification(notificationDto);

    // then
    verify(sseSender).sendToClientAsync(eq(userId), any(SseEmitter.class), eq(eventId), eq("notifications"), eq(notificationDto));
  }

  @DisplayName("cleanUpSseEmitters()는 연결이 되지 않는 SseEmitter가 있다면 userEmitters에서 삭제한다.")
  @Test
  void cleanUpSseEmitters_shouldRemoveUnconnectedSseEmitter() throws Exception {
    // given
    SseEmitter sseEmitter = new SseEmitter() {
      @Override
      public void send(@NotNull SseEventBuilder builder) throws IOException {
        throw new IOException();
      }
    };
    Map<Long, List<SseEmitter>> emitters = Map.of(100L, List.of(sseEmitter));
    given(sseRepository.findAllEmittersReadOnly()).willReturn(emitters);

    // when
    sseService.cleanUpSseEmitters();

    // then
    verify(sseRepository).removeEmitter(100L, sseEmitter);
  }

  @DisplayName("cleanUpSseInfos()는 오래된 SseInfo가 있다면 userEvents에서 삭제한다.")
  @Test
  void cleanUpSseInfos_shouldRemoveOldestEventIfExceedsLimit() {
    // given
    ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);

    // when
    sseService.cleanUpSseInfos();

    // then
    verify(sseRepository).cleanEventOlderThan(captor.capture());
  }
}

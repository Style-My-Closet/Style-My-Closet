package com.stylemycloset.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.stylemycloset.notification.dto.NotificationDto;
import com.stylemycloset.sse.dto.SseInfo;
import com.stylemycloset.sse.repository.SseRepository;
import com.stylemycloset.sse.service.SseSender;
import com.stylemycloset.sse.service.impl.SseServiceImpl;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
public class SseServiceUnitTest {

  @Mock
  private SseRepository sseRepository;

  @Mock
  private SseSender sseSender;

  @InjectMocks
  private SseServiceImpl sseService;

  private static final int MAX_EMITTER_COUNT = 3;
  private static final int MAX_EVENT_COUNT = 30;


  @DisplayName("Emitter개수가 MAX_EMITTER_COUNT이상이면 가장 오래된 emitter를 제거하고 저장한다.")
  @Test
  void connect_shouldRemoveOldestEmitterIfExceedsLimit() {
    // given
    Long userId = 9L;

    CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    for (int i = 0; i < MAX_EMITTER_COUNT; i++) {
      emitters.add(mock(SseEmitter.class));
    }
    SseEmitter oldest = emitters.getFirst();

    given(sseRepository.findByUserId(userId)).willReturn(emitters);

    // when
    SseEmitter newEmitter = sseService.connect(userId, String.valueOf(System.currentTimeMillis()), null);

    // then
    verify(oldest).complete();
    assertThat(emitters).contains(newEmitter);
    assertThat(emitters).doesNotContain(oldest);
  }

  @DisplayName("이벤트 개수가 MAX_EVENT_COUNT 이상이면 가장 오래된 이벤트를 제거한다")
  @Test
  void sendNotification_shouldRemoveOldestEventIfExceedsLimit() {
    // given
    Long userId = 10L;
    NotificationDto newNotification = mock(NotificationDto.class);
    given(newNotification.receiverId()).willReturn(userId);
    given(newNotification.createdAt()).willReturn(Instant.now());

    CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    emitters.add(mock(SseEmitter.class));
    given(sseRepository.findByUserId(userId)).willReturn(emitters);

    List<SseInfo> events = new CopyOnWriteArrayList<>();
    for (int i = 0; i < MAX_EVENT_COUNT; i++) {
      events.add(new SseInfo(i, "notifications", mock(NotificationDto.class), System.currentTimeMillis()));
    }

    ReflectionTestUtils.setField(sseService, "userEvents", new ConcurrentHashMap<>(
        Map.of(userId, events)));

    // when
    sseService.sendNotification(newNotification);

    // then
    Object field = ReflectionTestUtils.getField(sseService, "userEvents");
    @SuppressWarnings("unchecked")
    ConcurrentHashMap<Long, List<SseInfo>> userEvents =
        (ConcurrentHashMap<Long, List<SseInfo>>) Objects.requireNonNull(field);
    List<SseInfo> updatedList = userEvents.get(userId);

    assertThat(updatedList).hasSize(MAX_EVENT_COUNT);
    assertThat(updatedList.getFirst().id()).isEqualTo(1);
    assertThat(updatedList).extracting("id").doesNotContain(0L);
  }

  @DisplayName("sendNotification() 호출 시 userEvents에 이벤트가 저장된다")
  @Test
  void sendNotification_shouldStoreEventInUserEvents() {
    // given
    Long userId = 11L;

    NotificationDto notificationDto = mock(NotificationDto.class);
    given(notificationDto.receiverId()).willReturn(userId);
    given(notificationDto.createdAt()).willReturn(Instant.now());

    CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    emitters.add(mock(SseEmitter.class));
    given(sseRepository.findByUserId(userId)).willReturn(emitters);

    // when
    sseService.sendNotification(notificationDto);

    // then
    Object field = ReflectionTestUtils.getField(sseService, "userEvents");

    @SuppressWarnings("unchecked")
    ConcurrentHashMap<Long, List<SseInfo>> userEvents =
        (ConcurrentHashMap<Long, List<SseInfo>>) Objects.requireNonNull(field);

    List<SseInfo> storedEvents = userEvents.get(userId);
    assertThat(storedEvents).isNotNull();
    assertThat(storedEvents).hasSize(1);
    assertThat(storedEvents.getFirst().data()).isEqualTo(notificationDto);

    verify(sseSender).sendToClientAsync(
        eq(userId), any(SseEmitter.class),
        anyString(), eq("notifications"),
        eq(notificationDto)
    );
  }
}

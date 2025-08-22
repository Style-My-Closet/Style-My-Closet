package com.stylemycloset.sse.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.stylemycloset.sse.dto.SseInfo;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public class SseRepositoryUnitTest {

  SseRepository sseRepository;

  static final int MAX_EMITTER_COUNT = 3;
  static final int MAX_EVENT_COUNT = 30;
  Long userId = 100L;

  @BeforeEach
  void setUp() {
    sseRepository = new SseRepository();
  }

  @SuppressWarnings("unchecked")
  Deque<SseEmitter> getEmitters(Long userId) {
    Map<Long, Deque<SseEmitter>> userEmitters =
        (Map<Long, Deque<SseEmitter>>) ReflectionTestUtils.getField(sseRepository, "userEmitters");
    return userEmitters.getOrDefault(userId, new ArrayDeque<>());
  }

  @SuppressWarnings("unchecked")
  Deque<SseInfo> getEvents(Long userId) {
    Map<Long, Deque<SseInfo>> userEvents =
        (Map<Long, Deque<SseInfo>>) ReflectionTestUtils.getField(sseRepository, "userEvents");
    return userEvents.getOrDefault(userId, new ArrayDeque<>());
  }

  @DisplayName("Emitter 개수가 MAX_EMITTER_COUNT 미만이면 바로 Emitter를 저장하고 null을 반환한다.")
  @Test
  void addEmitter_whenBelowMax_shouldJustStore() {
    // given
    SseEmitter emitter = new SseEmitter();

    // when
    SseEmitter completedEmitter = sseRepository.addEmitter(userId, emitter);

    // then
    Deque<SseEmitter> emitters = getEmitters(userId);
    assertThat(completedEmitter).isNull();
    assertThat(emitters.getLast()).isSameAs(emitter);
  }

  @DisplayName("Emitter개수가 MAX_EMITTER_COUNT이상이면 가장 오래된 emitter를 제거하고 저장한다.")
  @Test
  void addEmitter_whenOverMax_shouldRemoveOldestAndAdd() {
    // given
    SseEmitter emitter1 = new SseEmitter();
    SseEmitter emitter2 = new SseEmitter();
    SseEmitter emitter3 = new SseEmitter();

    sseRepository.addEmitter(userId, emitter1);
    sseRepository.addEmitter(userId, emitter2);
    sseRepository.addEmitter(userId, emitter3);

    // when
    SseEmitter eNew = new SseEmitter(600_000L);
    SseEmitter completedEmitter = sseRepository.addEmitter(userId, eNew);

    // then
    assertThat(completedEmitter).isSameAs(emitter1);

    Deque<SseEmitter> emitters = getEmitters(userId);
    assertThat(emitters).hasSize(MAX_EMITTER_COUNT);
    assertThat(emitters.peekFirst()).isSameAs(emitter2);
    assertThat(emitters.peekLast()).isSameAs(eNew);

  }

  @DisplayName("이벤트 개수가 MAX_EVENT_COUNT 미만이면 바로 SseInfo를 저장한다.")
  @Test
  void addEvent_whenBelowMax_shouldJustStore() {
    // given
    SseInfo event = new SseInfo(1, "test", "test", 1);

    // when
    sseRepository.addEvent(userId, event);

    // then
    Deque<SseInfo> events = getEvents(userId);
    assertThat(events.getLast()).isSameAs(event);
  }

  @DisplayName("이벤트 개수가 MAX_EVENT_COUNT 이상이면 가장 오래된 이벤트를 제거하고 저장한다.")
  @Test
  void addEvent_whenOverMax_shouldRemoveOldestAndAdd() {
    // given
    for (int i = 0; i < MAX_EVENT_COUNT; i++) {
      sseRepository.addEvent(userId, new SseInfo(i, "test", "test" + i, i));
    }

    // when
    SseInfo newEvent = new SseInfo(30L, "test", "newTest", 3L);
    sseRepository.addEvent(userId, newEvent);

    // then
    Deque<SseInfo> events = getEvents(userId);
    assertThat(events).hasSize(MAX_EVENT_COUNT);
    assertThat(events.getLast()).isSameAs(newEvent);

    int expectedId = 1;
    for(SseInfo event : events) {
      assertThat(event.id()).isEqualTo(expectedId++);
    }
  }

  @DisplayName("같은 userId에 대해 여러 스레드가 동시에 addEvent()를 호출해도 예외가 발생하지 않고 데이터가 저장된다.")
  @Test
  void addEvent_parallelism_observed() throws Exception {
    // given
    int loops = 1000;

    ExecutorService pool = Executors.newFixedThreadPool(10);
    List<Future<?>> futures = new ArrayList<>();

    // when
    for(int i = 0; i < 1000; i++) {
      final long eventId = i;
      futures.add(pool.submit(() ->
        sseRepository.addEvent(userId, new SseInfo(eventId, "test", "test", 0))));
    }

    pool.shutdown();

    for(Future<?> future : futures) {
      future.get();
    }

    // then
    Deque<SseInfo> res = getEvents(userId);
    assertThat(res).hasSize(MAX_EVENT_COUNT);
    assertThat(res).extracting(SseInfo::id).doesNotHaveDuplicates();
  }

  @DisplayName("같은 userId에 대해 여러 스레드가 동시에 addEmitter()를 호출해도 예외가 발생하지 않고 데이터가 저장된다.")
  @Test
  void addEmitter_differentUserIds_parallelism_observed() throws Exception {

    ExecutorService pool = Executors.newFixedThreadPool(10);
    List<Future<?>> futures = new ArrayList<>();

    // when
    for(int i = 0; i < 1000; i++) {
      futures.add(pool.submit(() -> {
          SseEmitter emitter = new SseEmitter();
          sseRepository.addEmitter(userId, emitter);
      }));
    }

    pool.shutdown();

    for(Future<?> future : futures) {
      future.get();
    }

    Deque<SseEmitter> res = getEmitters(userId);
    assertThat(res).hasSize(MAX_EMITTER_COUNT);
  }

}

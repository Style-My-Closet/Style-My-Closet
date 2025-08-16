package com.stylemycloset.sse.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.stylemycloset.sse.dto.SseInfo;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
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
  Long userId2 = 101L;

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

  @DisplayName("같은 userId에 대해 동시 다발 addEvent()를 호출해도 30개만 정확히 남는다.")
  @Test
  void addEvent_sameUserId_concurrent_writes_keep_last_30() throws Exception {
    // given
    int total = 60;
    int threads = 20;

    ExecutorService pool = Executors.newFixedThreadPool(threads);
    CountDownLatch startGate = new CountDownLatch(1);
    CountDownLatch doneGate = new CountDownLatch(total);

    // when
    for (int i = 0; i < total; i++) {
      final long eventId = i + 1;
      pool.submit(() -> {
        try{
          startGate.await();
          sseRepository.addEvent(userId, new SseInfo(eventId, "test", "test", 0));
        } catch(InterruptedException e){
          Thread.currentThread().interrupt();
        } finally {
          doneGate.countDown();
        }
      });
    }

    startGate.countDown();
    doneGate.await();
    pool.shutdown();

    // then
    Deque<SseInfo> events = getEvents(userId);
    assertThat(events).hasSize(MAX_EVENT_COUNT);
  }

  @DisplayName("같은 userId에 대해 동시 다발 addEmitter()를 호출해도 마지막 3개만 정확히 남는다")
  @Test
  void addEmitter_sameUser_concurrent_keeps_last_3() throws Exception {
    int total = 6;
    int threads = 2;

    ExecutorService pool = Executors.newFixedThreadPool(threads);
    CountDownLatch startGate = new CountDownLatch(1);
    CountDownLatch doneGate = new CountDownLatch(total);

    for (int i = 0; i < total; i++) {
      pool.submit(() -> {
        try {
          startGate.await();
          sseRepository.addEmitter(userId, new SseEmitter());
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } finally {
          doneGate.countDown();
        }
      });
    }

    startGate.countDown();
    doneGate.await();
    pool.shutdown();

    Deque<SseEmitter> emitters = getEmitters(userId);
    assertThat(emitters).hasSize(MAX_EMITTER_COUNT);
  }

  @DisplayName("userEvents의 addEmitter()는 서로 다른 userId에 대해서 병렬로 처리된다.")
  @Test
  void addEvent_differentUserIds_parallelism_observed()  throws Exception {
    int loops = 31;

    AtomicInteger threadCount = new AtomicInteger();
    AtomicInteger maxThreadCount = new AtomicInteger();

    ExecutorService pool = Executors.newFixedThreadPool(2);
    CountDownLatch startGate = new CountDownLatch(1);
    CountDownLatch doneGate = new CountDownLatch(2);

    Runnable worker1 = makeEventWorker(userId, loops, threadCount, maxThreadCount, startGate, doneGate);
    Runnable worker2 = makeEventWorker(userId2, loops, threadCount, maxThreadCount, startGate, doneGate);

    pool.submit(worker1);
    pool.submit(worker2);
    startGate.countDown();
    doneGate.await();
    pool.shutdown();

    assertThat(maxThreadCount.get()).isEqualTo(2);
    assertThat(getEvents(userId)).hasSize(MAX_EVENT_COUNT);
    assertThat(getEvents(userId2)).hasSize(MAX_EVENT_COUNT);
  }

  @DisplayName("userEvents의 addEmitter()는 서로 다른 userId에 대해서 병렬로 처리된다.")
  @Test
  void addEmitter_differentUserIds_parallelism_observed() throws Exception {
    int loops = 31;

    AtomicInteger threadCount = new AtomicInteger();
    AtomicInteger maxThreadCount = new AtomicInteger();

    ExecutorService pool = Executors.newFixedThreadPool(2);
    CountDownLatch startGate = new CountDownLatch(1);
    CountDownLatch doneGate = new CountDownLatch(2);

    Runnable worker1 = makeEmitterWorker(userId, loops, threadCount, maxThreadCount, startGate, doneGate);
    Runnable worker2 = makeEmitterWorker(userId2, loops, threadCount, maxThreadCount, startGate, doneGate);

    pool.submit(worker1);
    pool.submit(worker2);
    startGate.countDown();
    doneGate.await();
    pool.shutdown();

    assertThat(maxThreadCount.get()).isEqualTo(2);
    assertThat(getEmitters(userId)).hasSize(MAX_EMITTER_COUNT);
    assertThat(getEmitters(userId2)).hasSize(MAX_EMITTER_COUNT);
  }

  private Runnable makeEventWorker(
      Long userId,
      int loops,
      AtomicInteger threadCount,
      AtomicInteger maxThreadCount,
      CountDownLatch startGate,
      CountDownLatch doneGate) {
    return () -> {
      try {
        startGate.await();
        for (int i = 0; i < loops; i++) {
          int now = threadCount.incrementAndGet();
          maxThreadCount.updateAndGet(m -> Math.max(m, now));
          try {
            sseRepository.addEvent(userId, new SseInfo(i + 1,"test","test",0));
          } finally {
            threadCount.decrementAndGet();
          }
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } finally {
        doneGate.countDown();
      }
    };
  }

  private Runnable makeEmitterWorker(Long userId,
      int loops,
      AtomicInteger threadCount,
      AtomicInteger maxThreadCount,
      CountDownLatch startGate,
      CountDownLatch doneGate) {
    return () -> {
      try {
        startGate.await();
        for (int i = 0; i < loops; i++) {
          int now = threadCount.incrementAndGet();
          maxThreadCount.updateAndGet(m -> Math.max(m, now));
          try {
            sseRepository.addEmitter(userId, new SseEmitter());
          } finally {
            threadCount.decrementAndGet();
          }
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } finally {
        doneGate.countDown();
      }
    };
  }


}

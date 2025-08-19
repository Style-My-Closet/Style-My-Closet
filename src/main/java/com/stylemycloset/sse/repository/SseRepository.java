package com.stylemycloset.sse.repository;

import com.stylemycloset.sse.dto.SseInfo;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class SseRepository {

  private final Map<Long, Deque<SseEmitter>> userEmitters;
  private final Map<Long, Deque<SseInfo>> userEvents;

  public SseRepository() {
    this.userEmitters = new ConcurrentHashMap<>();
    this.userEvents = new ConcurrentHashMap<>();
  }

  public SseRepository(
      Map<Long, Deque<SseEmitter>> userEmitters,
      Map<Long, Deque<SseInfo>> userEvents
  ) {
    this.userEmitters = userEmitters;
    this.userEvents = userEvents;
  }

  private static final int MAX_EMITTER_COUNT = 3;
  private static final int MAX_EVENT_COUNT = 30;

  public SseEmitter addEmitter(Long userId, SseEmitter emitter) {
    final SseEmitter[] completeEmitter = {null};
    userEmitters.compute(userId, (id, emitters) -> {
      Deque<SseEmitter> emitterList = (emitters == null) ? new ArrayDeque<>() : emitters;
      if(emitterList.size() >= MAX_EMITTER_COUNT) {
        completeEmitter[0] = emitterList.removeFirst();
      }
      emitterList.addLast(emitter);
      return emitterList;
    });

    return completeEmitter[0];
  }

  public void removeEmitter(Long userId, SseEmitter emitter) {
    userEmitters.computeIfPresent(userId, (k, emitters) -> {
      emitters.remove(emitter);
      return emitters.isEmpty() ? null : emitters;
    });
  }

  public Map<Long, List<SseEmitter>> findAllEmittersReadOnly() {
    Map<Long, List<SseEmitter>> result = new HashMap<>();
    for(Long userId : userEmitters.keySet()) {
      userEmitters.compute(userId, (id, emitters) -> {
        if(emitters != null && !emitters.isEmpty()) {
          result.put(id, List.copyOf(emitters));
        }
        return emitters;
      });
    }
    return Collections.unmodifiableMap(result);
  }

  public Deque<SseEmitter> findOrCreateEmitters(Long userId) {
    return userEmitters.computeIfAbsent(userId, k -> new ArrayDeque<>());
  }

  public void addEvent(Long userId, SseInfo event) {
    userEvents.compute(userId, (id, events) -> {
      Deque<SseInfo> eventQueue = (events == null) ? new ArrayDeque<>() : events;
      if(eventQueue.size() >= MAX_EVENT_COUNT) {
        eventQueue.removeFirst();
      }
      eventQueue.addLast(event);
      return eventQueue;
    });
  }

  public Deque<SseInfo> findOrCreateEvents(Long userId) {
    return userEvents.computeIfAbsent(userId, k -> new ArrayDeque<>());
  }

  public void cleanEventOlderThan(long timeout) {
    for(Long userId : userEvents.keySet()) {
      userEvents.compute(userId, (id, events) -> {
        if(events == null) return null;
        events.removeIf(event -> event.createdAt() < timeout);
        return events.isEmpty() ? null : events;
      });
    }
  }
}
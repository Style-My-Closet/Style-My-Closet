package com.stylemycloset.sse.repository;

import com.stylemycloset.sse.dto.SseInfo;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class SseRepository {

  private final ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> userEmitters = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Long, ConcurrentLinkedDeque<SseInfo>> userEvents = new ConcurrentHashMap<>();

  public void addEmitter(Long userId, SseEmitter emitter) {
    userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
  }

  public void removeEmitter(Long userId, SseEmitter emitter) {
    userEmitters.computeIfPresent(userId, (k, emitters) -> {
      emitters.remove(emitter);
      return emitters.isEmpty() ? null : emitters;
    });
  }

  public Map<Long, List<SseEmitter>> findAllEmittersReadOnly() {
    return userEmitters.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> Collections.unmodifiableList(entry.getValue())
        ));
  }

  public CopyOnWriteArrayList<SseEmitter> findOrCreateEmitters(Long userId) {
    return userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>());
  }

  public Deque<SseInfo> findOrCreateEvents(Long userId) {
    return userEvents.computeIfAbsent(userId, k -> new ConcurrentLinkedDeque<>());
  }

  public void cleanEventOlderThan(long timeout) {
    userEvents.forEach((userId, events) -> {
        events.removeIf(event -> event.createdAt() < timeout);
        if(events.isEmpty()) userEvents.remove(userId, events);
    });
  }
}
package com.stylemycloset.sse.repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class SseRepository {

  private final ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> userEmitters = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> weatherEmitters = new ConcurrentHashMap<>();

  public void save(Long userId, SseEmitter emitter) {
    userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
  }

  public void delete(Long userId, SseEmitter emitter) {
    userEmitters.computeIfPresent(userId, (k, emitters) -> {
      emitters.remove(emitter);
      return emitters.isEmpty() ? null : emitters;
    });
  }

  public Map<Long, List<SseEmitter>> getAllEmittersReadOnly() {
    return userEmitters.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> Collections.unmodifiableList(entry.getValue())
        ));
  }


  public Optional<Long> getUserIdByEmitter(SseEmitter emitter) {
    return userEmitters.entrySet().stream()
        .filter(entry -> entry.getValue().contains(emitter))
        .map(Map.Entry::getKey)
        .findFirst();
  }

}

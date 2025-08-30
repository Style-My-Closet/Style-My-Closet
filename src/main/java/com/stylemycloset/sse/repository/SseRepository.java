package com.stylemycloset.sse.repository;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
@Slf4j
@Repository
public class SseRepository {

  private final ConcurrentHashMap<Long, Deque<SseEmitter>> userEmitters = new ConcurrentHashMap<>();
  private static final int MAX_EMITTER_COUNT = 3;

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
    Deque<SseEmitter> original = userEmitters.computeIfAbsent(userId, k -> new ArrayDeque<>());
    return new ArrayDeque<>(original);
  }
}
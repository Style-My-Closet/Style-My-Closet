package com.stylemycloset.sse.repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class SseRepository {

  private final ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

  public void save(Long userId, SseEmitter emitter) {
    userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
  }

  public void delete(Long userId, SseEmitter emitter) {
    if(!userEmitters.containsKey(userId)) return;

    List<SseEmitter> emitters = userEmitters.get(userId);
    emitters.remove(emitter);
    if(emitters.isEmpty()) userEmitters.remove(userId);
  }

  public Map<Long, CopyOnWriteArrayList<SseEmitter>> findAllEmitters() {
    Map<Long, CopyOnWriteArrayList<SseEmitter>> map = new ConcurrentHashMap<>();
    userEmitters.forEach((userId, emitters) ->
      map.put(userId, new CopyOnWriteArrayList<>(emitters))
    );
    return map;
  }
}

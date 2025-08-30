package com.stylemycloset.sse.event;

import com.stylemycloset.sse.dto.NotificationTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisPublishListener {

  private final RedisTemplate<String, Object> redisTemplate;

  public void publish(NotificationTarget message) {
    redisTemplate.convertAndSend("notification-event", message);
  }
}

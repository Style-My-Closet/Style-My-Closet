package com.stylemycloset.security.jwt;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtBlacklist {

  private final RedisTemplate<String, Object> redisTemplate;

  public void putToken(String token, Duration expirationTime) {
    redisTemplate.opsForValue().set(token, "invalidate", expirationTime);
  }

  public boolean isBlacklisted(String accessToken) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(accessToken));
  }

}

package com.stylemycloset.common.config;

import com.stylemycloset.sse.event.RedisSubscriber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisPubSubConfig {

  private final RedisConnectionFactory connectionFactory;
  private final RedisSubscriber subscriber;

  @Bean
  public RedisMessageListenerContainer redisMessageListenerContainer() {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);

    container.addMessageListener(subscriber, new ChannelTopic("notification-event"));
    container.setErrorHandler(e -> log.error("Redis Listener Error", e));
    return container;
  }
}

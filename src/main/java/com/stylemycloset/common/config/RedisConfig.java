package com.stylemycloset.common.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class RedisConfig {
  @Value("${spring.data.redis.host}")
  private String host;

  @Value("${spring.data.redis.port}")
  private int port;

  @Value("${spring.data.redis.ssl.enabled}")
  private boolean ssl;

  public static final String CLOTHES_CACHE = "clothes";
  public static final String CLOTHES_ATTRIBUTE_CACHE = "clothesAttribute";
  public static final String CLOTHES_ATTRIBUTES_KEY = "'allClothesAttribute'";

  private final ObjectMapper objectMapper;

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    RedisStandaloneConfiguration conf = new RedisStandaloneConfiguration(host, port);

    var builder = LettuceClientConfiguration.builder();
    if (ssl) {
      builder.useSsl();
    }

    return new LettuceConnectionFactory(conf, builder.build());
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate() {
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory());

    StringRedisSerializer keySerializer = new StringRedisSerializer();
    GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

    redisTemplate.setKeySerializer(keySerializer);
    redisTemplate.setHashKeySerializer(keySerializer);
    redisTemplate.setValueSerializer(valueSerializer);
    redisTemplate.setHashValueSerializer(valueSerializer);
    redisTemplate.afterPropertiesSet();

    return redisTemplate;
  }

  @Bean
  public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
    var keySerializer = RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer());
    var valueSerializer = RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer());

    RedisCacheConfiguration conf = RedisCacheConfiguration
        .defaultCacheConfig()
        .serializeKeysWith(keySerializer)
        .serializeValuesWith(valueSerializer)
        .disableCachingNullValues();

    Map<String, RedisCacheConfiguration> confMap = new HashMap<>();
    confMap.put(CLOTHES_CACHE, conf.entryTtl(Duration.ofMinutes(8)));
    confMap.put(CLOTHES_ATTRIBUTE_CACHE, conf.entryTtl(Duration.ofMinutes(10)));

    return RedisCacheManager.builder(redisConnectionFactory)
        .cacheDefaults(conf)
        .withInitialCacheConfigurations(confMap)
        .transactionAware()
        .build();
  }

  @Bean
  public RedisScript<Long> redisScript() {
    Resource script = new ClassPathResource("redis/redis-trim.lua");
    return RedisScript.of(script, Long.class);
  }
}

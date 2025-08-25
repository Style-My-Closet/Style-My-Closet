package com.stylemycloset.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.List;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheManagerProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

  public static final String CLOTHES_CACHE = "clothes";
  public static final String CLOTHES_ATTRIBUTE_CACHE = "clothesAttribute";
  public static final String CLOTHES_ATTRIBUTES_KEY = "'allClothesAttribute'";

  @Bean
  public CacheManager cacheManager() {
    CaffeineCache clothes = new CaffeineCache(
        CLOTHES_CACHE,
        Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(2))
            .maximumSize(10_000)
            .recordStats()
            .build()
    );

    CaffeineCache clothesAttribute = new CaffeineCache(
        CLOTHES_ATTRIBUTE_CACHE,
        Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10))
            .maximumSize(10_000)
            .recordStats()
            .build()
    );

    SimpleCacheManager delegate = new SimpleCacheManager();
    delegate.setCaches(List.of(clothes, clothesAttribute));
    delegate.afterPropertiesSet();

    return new TransactionAwareCacheManagerProxy(delegate);
  }

}

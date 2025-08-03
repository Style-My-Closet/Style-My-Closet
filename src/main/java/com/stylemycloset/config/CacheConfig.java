package com.stylemycloset.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("clothCount", "attributeCount", "attributeOptions");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(10000) // 최대 10,000개 캐시 엔트리
                .expireAfterWrite(Duration.ofHours(1)) // 1시간 후 만료
                .expireAfterAccess(Duration.ofMinutes(30))); // 30분 미접근 시 만료
        
        return cacheManager;
    }
}

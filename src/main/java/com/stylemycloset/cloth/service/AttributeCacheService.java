package com.stylemycloset.cloth.service;

import com.stylemycloset.cloth.repository.ClothingAttributeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AttributeCacheService {

    private final CacheManager cacheManager;
    private final ClothingAttributeRepository clothingAttributeRepository;
    
    private final ConcurrentHashMap<String, Object> cacheLocks = new ConcurrentHashMap<>();

    @Transactional(readOnly = true)
    @Cacheable(value = "attributeCount", key = "'total'")
    public Long getAttributeCount() {
        return getAttributeCountInternal();
    }
    
    // 내부 호출용 메서드 (캐시 어노테이션 없음)
    private Long getAttributeCountInternal() {
        try {
            Cache cache = cacheManager.getCache("attributeCount");
            if (cache != null) {
                Cache.ValueWrapper wrapper = cache.get("total");
                if (wrapper != null) {
                    return (Long) wrapper.get();
                }
            }
            
            // 캐시에 없으면 DB에서 조회하고 캐시에 저장
            long dbCount = clothingAttributeRepository.countByKeyword(null);
            setAttributeCount(dbCount);
            return dbCount;
        } catch (Exception e) {
            log.warn("캐시 조회 실패, 데이터베이스에서 조회합니다.", e);
            return null;
        }
    }

    public void setAttributeCount(Long count) {
        try {
            Cache cache = cacheManager.getCache("attributeCount");
            if (cache != null) {
                cache.put("total", count);
            }
        } catch (Exception e) {
            log.warn("캐시 저장 실패. count: {}", count, e);
        }
    }

    public void evictAttributeCount() {
        try {
            Cache cache = cacheManager.getCache("attributeCount");
            if (cache != null) {
                cache.evict("total");
            }
        } catch (Exception e) {
            log.warn("캐시 무효화 실패.", e);
        }
    }
    
    public void incrementAttributeCount() {
        updateAttributeCount(true);
    }
    
    public void decrementAttributeCount() {
        updateAttributeCount(false);
    }
    
    private void updateAttributeCount(boolean increment) {
        Object lock = cacheLocks.computeIfAbsent("attributeCount", k -> new Object());
        
        synchronized (lock) {
            try {
                // 직접 캐시에서 조회 (자기 호출 방지)
                Long currentCount = getCachedAttributeCount();
                
                if (currentCount != null) {
                    // 캐시에 값이 있으면 증감
                    long newCount = increment ? currentCount + 1 : Math.max(0, currentCount - 1);
                    setAttributeCount(newCount);
                } else {
                    // 캐시에 값이 없으면 DB에서 조회 후 증감
                    long dbCount = clothingAttributeRepository.countByKeyword(null);
                    long newCount = increment ? dbCount + 1 : Math.max(0, dbCount - 1);
                    setAttributeCount(newCount);
                }
            } catch (Exception e) {
                evictAttributeCount();
                log.warn("캐시 업데이트 실패, 캐시 무효화 처리.", e);
            }
        }
    }
    
    // 캐시에서 직접 조회
    private Long getCachedAttributeCount() {
        try {
            Cache cache = cacheManager.getCache("attributeCount");
            if (cache != null) {
                Cache.ValueWrapper wrapper = cache.get("total");
                if (wrapper != null) {
                    return (Long) wrapper.get();
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("캐시 조회 실패.", e);
            return null;
        }
    }
} 
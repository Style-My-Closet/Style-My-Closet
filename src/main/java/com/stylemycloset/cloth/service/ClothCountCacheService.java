package com.stylemycloset.cloth.service;

import com.stylemycloset.cloth.repository.ClothRepository;
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
public class ClothCountCacheService {

    private final CacheManager cacheManager;
    private final ClothRepository clothRepository;
    
    // 캐시 락을 위한 맵
    private final ConcurrentHashMap<Long, Object> userCacheLocks = new ConcurrentHashMap<>();

    @Transactional(readOnly = true)
    @Cacheable(value = "clothCount", key = "#userId != null ? 'user:' + #userId : 'total'")
    public Long getUserClothCount(Long userId) {
        return getUserClothCountInternal(userId);
    }
    
    private Long getUserClothCountInternal(Long userId) {
        try {
            if (userId == null) {
                // 전체 카운트는 캐시 없이 직접 조회
                return clothRepository.count();
            }
            
            Cache cache = cacheManager.getCache("clothCount");
            if (cache != null) {
                Cache.ValueWrapper wrapper = cache.get("user:" + userId);
                if (wrapper != null) {
                    return (Long) wrapper.get();
                }
            }
            
            // 캐시에 없으면 DB에서 조회하고 캐시에 저장
            long dbCount = clothRepository.countByUserId(userId);
            setUserClothCount(userId, dbCount);
            return dbCount;
        } catch (Exception e) {
            log.warn("캐시 조회 실패, 데이터베이스에서 조회합니다. userId: {}", userId, e);
            return null;
        }
    }

    public void setUserClothCount(Long userId, Long count) {
        try {
            Cache cache = cacheManager.getCache("clothCount");
            if (cache != null) {
                String key = userId != null ? "user:" + userId : "total";
                cache.put(key, count);
            }
        } catch (Exception e) {
            log.warn("캐시 저장 실패. userId: {}, count: {}", userId, count, e);
        }
    }

    public void evictUserClothCount(Long userId) {
        try {
            Cache cache = cacheManager.getCache("clothCount");
            if (cache != null) {
                String key = userId != null ? "user:" + userId : "total";
                cache.evict(key);
            }
        } catch (Exception e) {
            log.warn("캐시 무효화 실패");
        }
    }
    
    public void incrementUserClothCount(Long userId) {
        updateUserClothCount(userId, true);
    }
    
    public void decrementUserClothCount(Long userId) {
        updateUserClothCount(userId, false);
    }
    
    private void updateUserClothCount(Long userId, boolean increment) {
        if (userId == null) {
           
            return;
        }
        
        Object lock = userCacheLocks.computeIfAbsent(userId, k -> new Object());
        
        synchronized (lock) {
            try {
                // 직접 캐시에서 조회 (자기 호출 방지)
                Long currentCount = getCachedUserClothCount(userId);
                
                if (currentCount != null) {
                    // 캐시에 값이 있으면 증감
                    long newCount = increment ? currentCount + 1 : Math.max(0, currentCount - 1);
                    setUserClothCount(userId, newCount);
                } else {
                    // 캐시에 값이 없으면 DB에서 조회 후 증감
                    long dbCount = clothRepository.countByUserId(userId);
                    long newCount = increment ? dbCount + 1 : Math.max(0, dbCount - 1);
                    setUserClothCount(userId, newCount);
                }
            } catch (Exception e) {
                evictUserClothCount(userId);
                log.warn("캐시 업데이트 실패, 캐시 무효화 처리. userId: {}", userId, e);
            }
        }
    }
    
    // 캐시에서 직접 조회하는 메서드 (내부 호출용)
    private Long getCachedUserClothCount(Long userId) {
        try {
            if (userId == null) {
                return null; // 전체 카운트는 증감 대상이 아님
            }
            
            Cache cache = cacheManager.getCache("clothCount");
            if (cache != null) {
                Cache.ValueWrapper wrapper = cache.get("user:" + userId);
                if (wrapper != null) {
                    return (Long) wrapper.get();
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("캐시 조회 실패. userId: {}", userId, e);
            return null;
        }
    }
} 
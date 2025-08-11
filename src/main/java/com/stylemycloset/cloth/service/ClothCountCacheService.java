package com.stylemycloset.cloth.service;

import com.stylemycloset.cloth.repository.ClothRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClothCountCacheService {

    private final ClothRepository clothRepository;
    
    @Transactional(readOnly = true)
    @Cacheable(value = "clothCount", key = "#userId != null ? 'user:' + #userId : 'total'")
    public Long getUserClothCount(Long userId) {
        if (userId == null) {
            return clothRepository.count();
        }
        return clothRepository.countByUserId(userId);
    }

    @CacheEvict(value = "clothCount", key = "#userId != null ? 'user:' + #userId : 'total'")
    public void evictUserClothCount(Long userId) {}
} 
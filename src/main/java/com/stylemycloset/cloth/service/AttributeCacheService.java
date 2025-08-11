package com.stylemycloset.cloth.service;

import com.stylemycloset.cloth.repository.ClothingAttributeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttributeCacheService {

    private final ClothingAttributeRepository clothingAttributeRepository;
    
    @Transactional(readOnly = true)
    @Cacheable(value = "attributeCount", key = "'total'")
    public Long getAttributeCount() {
        return clothingAttributeRepository.countByKeyword(null);
    }
    
    @CacheEvict(value = "attributeCount", key = "'total'")
    public void evictAttributeCount() {
    }
} 
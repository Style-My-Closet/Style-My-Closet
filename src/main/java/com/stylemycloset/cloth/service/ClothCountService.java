package com.stylemycloset.cloth.service;

import com.stylemycloset.cloth.repository.ClothRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
// 프록시로 인해 별로 분리
public class ClothCountService {

    private final ClothRepository clothRepository;

    @Cacheable(value = "clothCount", key = "#userId")
    @Transactional(readOnly = true)
    public long getUserClothCount(Long userId) {
        if (userId == null) {
            return clothRepository.count();
        }
        return clothRepository.countByUserId(userId);

    }
}

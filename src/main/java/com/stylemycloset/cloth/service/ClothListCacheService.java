package com.stylemycloset.cloth.service;

import com.stylemycloset.cloth.dto.CursorDto;
import com.stylemycloset.cloth.dto.SortDirection;
import com.stylemycloset.cloth.dto.SortField;
import com.stylemycloset.cloth.dto.response.PaginatedResponse;
import com.stylemycloset.cloth.dto.response.ClothListResponseDto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.core.env.Environment;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClothListCacheService {

    private final Environment environment;
    @Getter
    private final boolean cachingEnabled = initCachingEnabled();

    private boolean initCachingEnabled() {
        String[] profiles = (environment != null) ? environment.getActiveProfiles() : null;
        if (profiles == null) return true;
        for (String p : profiles) {
            if ("test".equalsIgnoreCase(p)) {
                return false;
            }
        }
        return true;
    }

    // 옷 목록 첫 페이지 캐시
    @Cacheable(value = "clothListFirstPage", key = "#userId != null ? 'user:' + #userId : 'total'", condition = "#root.target.isCachingEnabled()")
    public ClothListResponseDto getClothListFirstPage(Long userId, Supplier<ClothListResponseDto> supplier) {
        return supplier.get();
    }

    @CacheEvict(value = "clothListFirstPage", key = "#userId != null ? 'user:' + #userId : 'total'", condition = "#root.target.isCachingEnabled()")
    public void evictClothListFirstPage(Long userId) {
    }

    // 속성 목록 첫 페이지 캐시
    @Cacheable(value = "attributeListFirstPage", key = "'first:page'", condition = "#root.target.isCachingEnabled()")
    public PaginatedResponse<com.stylemycloset.cloth.dto.ClothesAttributeDefDto> getAttributeListFirstPage(Supplier<PaginatedResponse<com.stylemycloset.cloth.dto.ClothesAttributeDefDto>> supplier) {
        return supplier.get();
    }

    @CacheEvict(value = "attributeListFirstPage", allEntries = true, condition = "#root.target.isCachingEnabled()")
    public void evictAttributeListFirstPage() {
    }


    public boolean isFirstPage(CursorDto cursorDto) {
        return cursorDto.cursor() == null && cursorDto.idAfter() == null;
    }
    public boolean isDefaultSort(CursorDto cursorDto) {
        return SortField.CREATED_AT.getValue().equals(cursorDto.sortBy()) &&
               SortDirection.DESCENDING.getValue().equals(cursorDto.sortDirection());
    }
    public boolean isNoKeywordSearch(CursorDto cursorDto) {
        return cursorDto.keywordLike() == null || cursorDto.keywordLike().trim().isEmpty();
    }
} 
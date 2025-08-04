package com.stylemycloset.cloth.service;

import com.stylemycloset.binarycontent.BinaryContent;
import com.stylemycloset.binarycontent.BinaryContentRepository;
import com.stylemycloset.cloth.dto.ClothCreateRequestDto;
import com.stylemycloset.cloth.dto.ClothResponseDto;
import com.stylemycloset.cloth.dto.CursorDto;
import com.stylemycloset.cloth.dto.SortDirection;
import com.stylemycloset.cloth.dto.response.ClothItemDto;
import com.stylemycloset.cloth.dto.response.ClothListResponseDto;
import com.stylemycloset.cloth.dto.response.ClothUpdateResponseDto;
import com.stylemycloset.cloth.entity.*;
import com.stylemycloset.cloth.exception.ClothesException;
import com.stylemycloset.cloth.exception.ClothingErrorCode;
import com.stylemycloset.cloth.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClothService {
    private final ClothRepository clothRepository;
    private final ClothingCategoryRepository categoryRepository;
    private final ClosetRepository closetRepository;
    private final BinaryContentRepository binaryContentRepository;

    private final CacheManager cacheManager;
    private final ClothCountService clothCountService;

    // 캐시 락을 위한 맵
    private final ConcurrentHashMap<Long, Object> userCacheLocks = new ConcurrentHashMap<>();



    @Transactional(readOnly = true)
    public ClothListResponseDto getClothesWithCursor(Long userId, CursorDto cursorDto) {
        int limit = cursorDto.limit() != null ? Integer.parseInt(cursorDto.limit()) : 20;
        String sortBy = cursorDto.sortBy() != null ? cursorDto.sortBy() : "id";
        boolean isDescending = !"asc".equalsIgnoreCase(cursorDto.sortDirection());
        Long idAfter = cursorDto.idAfter() != null ? Long.valueOf(cursorDto.idAfter()) : null;
        boolean hasIdAfter = (idAfter != null);


        List<Cloth> clothes = clothRepository.findWithCursorPagination(
                userId, idAfter, limit + 1, isDescending, hasIdAfter);

        //limit+1 보다 크다면 다음값이 있음
        boolean hasNext = clothes.size() > limit;
        if (hasNext) {
            clothes = clothes.subList(0, limit);
        }

        String lastId = (hasNext && !clothes.isEmpty())
                ? clothes.getLast().getId().toString()
                : null;


        long totalCount = clothCountService.getUserClothCount(userId);


        List<ClothItemDto> data = clothes.stream()
                .map(ClothItemDto::new)
                .toList();

        return ClothListResponseDto.builder()
                .data(data)
                .nextCursor(lastId)
                .nextIdAfter(lastId)
                .hasNext(hasNext)
                .totalCount(totalCount)
                .sortBy(sortBy)
                .sortDirection(isDescending ? SortDirection.DESCENDING : SortDirection.ASCENDING)
                .build();
    }



    @Transactional
    public ClothResponseDto createCloth(ClothCreateRequestDto requestDto, Long userId, MultipartFile image) {
        Closet closet = getClosetByUserId(userId);
        ClothingCategory category = getCategoryById(requestDto.getCategoryId());
        BinaryContent binaryContent = processImage(image, requestDto.getBinaryContent());

        Cloth savedCloth = saveCloth(requestDto, closet, category, binaryContent);


        if(TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        incrementCount(userId);
                    } catch (Exception e) {
                        evictUserClothCountCache(userId);
                    }
                }
            });
        }

        return new ClothResponseDto(savedCloth);
    }
    
    @Transactional
    public ClothUpdateResponseDto updateCloth(Long clothId, ClothCreateRequestDto requestDto, MultipartFile image) {
        Cloth cloth = getClothById(clothId);
        
        updateCloth(cloth, requestDto, image);
        


        Cloth updatedCloth = clothRepository.findByIdWithAttributes(clothId)
                .orElseThrow(() -> new ClothesException(ClothingErrorCode.CLOTH_UPDATE_FAILED));

        return ClothUpdateResponseDto.from(updatedCloth);
    }
    
    @Transactional
    public void deleteCloth(Long clothId) {
        Cloth cloth = getClothById(clothId);
        Long userId = cloth.getCloset().getUser().getId();
        
        clothRepository.deleteById(clothId);
        if(TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        decrementClothCount(userId);
                    } catch (Exception e) {
                        log.warn("캐시 업데이트 실패, 캐시 무효화 처리. userId: {}", userId, e);
                        evictUserClothCountCache(userId);
                    }
                }
            });
        }
    }


    private void evictUserClothCountCache(Long userId) {
        try {
            Cache cache = cacheManager.getCache("clothCount");
            if (cache != null) {
                cache.evict(userId);

            }
        } catch (Exception e) {
            log.error("캐시 무효화 실패. userId: {}", userId, e);
        }
    }

    //aop로 같은 트랜젝션에 묶이지 않음 만약 롤백된다면서 캐시도 같이 롤백해 줘야함
    private void incrementCount(Long userId) {
        Object lock = userCacheLocks.computeIfAbsent(userId, k -> new Object());
        
        synchronized (lock) {
            try {
                Cache cache = cacheManager.getCache("clothCount");
                if (cache != null) {
                    Cache.ValueWrapper wrapper = cache.get(userId);
                    if (wrapper != null) {
                        Long currentCount = (Long) wrapper.get();
                        if (currentCount != null) {
                            cache.put(userId, currentCount + 1);
                        } else {
                            long dbCount = clothRepository.countByUserId(userId);
                            cache.put(userId, dbCount + 1);
                        }
                    } else {
                        long dbCount = clothRepository.countByUserId(userId);
                        cache.put(userId, dbCount + 1);
                    }
                }
            } catch (Exception e) {
                // 캐시 업데이트 실패 시 캐시 무효화
                evictUserClothCountCache(userId);
                throw e;
            }
        }
    }

    private void decrementClothCount(Long userId) {
        Object lock = userCacheLocks.computeIfAbsent(userId, k -> new Object());
        
        synchronized (lock) {
            try {
                Cache cache = cacheManager.getCache("clothCount");
                if (cache != null) {
                    Cache.ValueWrapper wrapper = cache.get(userId);
                    if (wrapper != null) {
                        Long currentCount = (Long) wrapper.get();
                        if (currentCount != null) {
                            // 음수 방지
                            cache.put(userId, Math.max(0, currentCount - 1));
                        } else {
                            long dbCount = clothRepository.countByUserId(userId);
                            cache.put(userId, Math.max(0, dbCount - 1));
                        }
                    } else {
                        // 캐시 미스면 DB에서 조회 후 캐싱
                        long dbCount = clothRepository.countByUserId(userId);
                        cache.put(userId, Math.max(0, dbCount - 1));
                    }
                }
            } catch (Exception e) {
                // 캐시 업데이트 실패 시 캐시 무효화
                evictUserClothCountCache(userId);
                throw e;
            }
        }
    }
    

    private Closet getClosetByUserId(Long userId) {
        return closetRepository.findByUserId(userId)
                .orElseThrow(() -> new ClothesException(ClothingErrorCode.CLOSET_NOT_FOUND));
    }
    
    private ClothingCategory getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ClothesException(ClothingErrorCode.CATEGORY_NOT_FOUND));
    }
    
    private BinaryContent getBinaryContentById(UUID binaryContentId) {
        return binaryContentRepository.findById(binaryContentId)
                .orElseThrow(RuntimeException::new);
    }
    
    private Cloth getClothById(Long clothId) {
        return clothRepository.findById(clothId)
                .orElseThrow(() -> new ClothesException(ClothingErrorCode.CLOTH_NOT_FOUND));
    }
    
    private Cloth saveCloth(ClothCreateRequestDto requestDto, Closet closet,
                            ClothingCategory category, BinaryContent binaryContent) {
        Cloth cloth = Cloth.createCloth(requestDto.getName(), closet, category, binaryContent);
        
        return clothRepository.save(cloth);
    }
    

    
    private void updateCloth(Cloth cloth, ClothCreateRequestDto requestDto, MultipartFile image) {
        if (requestDto.getName() != null) {
            cloth.updateName(requestDto.getName());
        }
        
        if (requestDto.getCategoryId() != null) {
            ClothingCategory category = getCategoryById(requestDto.getCategoryId());
            cloth.updateCategory(category);
        }
        
        // 이미지 업데이트 처리
        if (image != null || requestDto.getBinaryContent() != null) {
            BinaryContent binaryContent = processImage(image, requestDto.getBinaryContent());
            cloth.updateBinaryContent(binaryContent);
        }
    }
    
    private BinaryContent processImage(MultipartFile image, UUID existingBinaryContentId) {
        if (image != null && !image.isEmpty()) {
            // 이미지 서비스 구현 x
           return null;
        }
        
        if (existingBinaryContentId != null) {
            return getBinaryContentById(existingBinaryContentId);
        }
        
        throw new IllegalArgumentException("이미지 파일 또는 기존 이미지 ID가 필요합니다.");
    }


}

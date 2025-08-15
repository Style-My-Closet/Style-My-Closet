package com.stylemycloset.cloth.service;

import com.stylemycloset.cloth.dto.ClothesAttributeDefCreateRequest;
import com.stylemycloset.cloth.dto.ClothesAttributeDefDto;
import com.stylemycloset.cloth.dto.CursorDto;
import com.stylemycloset.cloth.dto.response.PaginatedResponse;
import com.stylemycloset.cloth.dto.response.AttributeResponseDto;
import com.stylemycloset.cloth.dto.SortDirection;
import com.stylemycloset.cloth.entity.AttributeOption;
import com.stylemycloset.cloth.entity.ClothingAttribute;
import com.stylemycloset.cloth.entity.ClothingAttributeValue;
import com.stylemycloset.cloth.exception.ClothesException;
import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.cloth.repository.ClothingAttributeRepository;
import com.stylemycloset.cloth.repository.ClothingAttributeValueRepository;
import com.stylemycloset.cloth.mapper.AttributeResponseMapper;
import com.stylemycloset.cloth.mapper.ClothesAttributeDefMapper;
import lombok.RequiredArgsConstructor;
import com.stylemycloset.notification.event.domain.NewClothAttributeEvent;
import com.stylemycloset.notification.event.domain.ClothAttributeChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClothAttributeService {

    private final ClothingAttributeRepository clothingAttributeRepository;
    private final ClothingAttributeValueRepository clothingAttributeValueRepository;
    private final AttributeCacheService attributeCacheService;
    private final ClothListCacheService clothListCacheService;
    private final AttributeResponseMapper attributeResponseMapper;
    private final ClothesAttributeDefMapper clothesAttributeDefMapper;
    private final ApplicationEventPublisher eventPublisher;


    @Transactional(readOnly = true)
    public PaginatedResponse<ClothesAttributeDefDto> findAttributes(CursorDto cursorDto) {
        if (clothListCacheService.isFirstPage(cursorDto) && clothListCacheService.isNoKeywordSearch(cursorDto)) {
            return clothListCacheService.getAttributeListFirstPage(
                () -> computeAttributeListResponse(cursorDto)
            );
        }
        
        return computeAttributeListResponse(cursorDto);
    }
    

    private PaginatedResponse<ClothesAttributeDefDto> computeAttributeListResponse(CursorDto cursorDto) {
        String keywordLike = cursorDto.keywordLike();
        Long cursor = cursorDto.cursor();
        int size = cursorDto.limit();
        String sortBy = cursorDto.sortBy();
        String sortDirection = cursorDto.sortDirection();

        List<ClothingAttribute> attributes = clothingAttributeRepository.findWithCursorPagination(
                keywordLike, cursor, size
        );

        if (attributes == null) {
            attributes = new ArrayList<>();
        }
        List<ClothesAttributeDefDto> data = clothesAttributeDefMapper.toDtoList(attributes);
        
        long totalCount = getAttributeCount(keywordLike);
        boolean hasNext = attributes.size() == size;
        String nextCursor = hasNext && !attributes.isEmpty() ?
                attributes.getLast().getId().toString() : null;

        return PaginatedResponse.of(
                data,
                nextCursor,
                nextCursor,
                hasNext,
                totalCount,
                sortBy,
                sortDirection != null ? SortDirection.fromString(sortDirection) : SortDirection.ASCENDING
        );
    }

    @Transactional
    public AttributeResponseDto createAttribute(ClothesAttributeDefCreateRequest request) {
        validateCreateRequest(request);
        ClothingAttribute savedAttribute = persistNewAttribute(request);
        evictAttributeCachesAfterCommit();
        publishNewAttributeEvent(savedAttribute);
        return attributeResponseMapper.toDto(savedAttribute);
    }

    @Transactional
    public void deleteAttributeById(Long id) {
        ClothingAttribute attribute = getAttributeOrThrow(id);
        softDeleteAttributeWithValues(attribute);
        evictAttributeCachesAfterCommit();
    }

    @Transactional
    public AttributeResponseDto updateAttribute(Long id, ClothesAttributeDefCreateRequest request) {
        // 1. 입력값 검증
        if (id == null || request == null || request.name() == null) {
            throw new ClothesException(ErrorCode.INVALID_PARAMETER);
        }
        
        // 2. 속성 조회
        ClothingAttribute attribute = clothingAttributeRepository.findById(id)
                .orElseThrow(() -> new ClothesException(ErrorCode.ATTRIBUTE_NOT_FOUND));
        
        // 3. 속성 이름 업데이트
        attribute.updateName(request.name());
        
        // 4. 옵션 값들 정규화
        List<String> targetValues = normalizeValues(extractTargetValues(request));
        
        // 5. 기존 옵션 중 없어진 것들 삭제
        softDeleteMissingOptions(attribute.getId(), targetValues);
        
        // 6. 속성 다시 로드 (삭제된 옵션 반영)
        ClothingAttribute refreshedAttribute = reloadAttribute(attribute.getId(), attribute);
        
        // 7. 새로운 옵션들 추가
        addMissingOptions(refreshedAttribute, targetValues);
        
        // 8. 속성 저장
        ClothingAttribute savedAttribute = clothingAttributeRepository.save(refreshedAttribute);
        
        // 9. 캐시 무효화
        evictAttributeListCacheAfterCommit();
        
        // 10. 변경 이벤트 발행
        publishAttributeChangedEvent(savedAttribute);
        
        // 11. DTO로 변환하여 반환
        return attributeResponseMapper.toDto(savedAttribute);
    }

    @Transactional
    public AttributeResponseDto addAttributeOptions(Long id, List<String> optionValues) {
        // 1. 입력값 검증
        if (id == null || optionValues == null || optionValues.isEmpty()) {
            throw new ClothesException(ErrorCode.INVALID_PARAMETER);
        }
        
        // 2. 속성 조회
        ClothingAttribute attribute = clothingAttributeRepository.findById(id)
                .orElseThrow(() -> new ClothesException(ErrorCode.ATTRIBUTE_NOT_FOUND));
        
        // 3. 옵션 값들 정규화하여 추가
        List<String> normalizedValues = normalizeValues(optionValues);
        attribute.addOptions(normalizedValues);
        
        // 4. 저장 및 캐시 무효화 후 반환
        return saveAndEvictAfterCommit(attribute);
    }

    @Transactional
    public void removeAttributeOptions(Long id, List<String> optionValues) {
        // 1. 입력값 검증
        if (id == null || optionValues == null || optionValues.isEmpty()) {
            throw new ClothesException(ErrorCode.INVALID_PARAMETER);
        }
        
        // 2. 속성 조회
        ClothingAttribute attribute = clothingAttributeRepository.findById(id)
                .orElseThrow(() -> new ClothesException(ErrorCode.ATTRIBUTE_NOT_FOUND));
        
        // 3. 옵션 값들 정규화
        List<String> normalizedValues = normalizeValues(optionValues);
        
        // 4. 삭제할 옵션들의 AttributeValue들도 함께 삭제 (단방향 관계로 인해 수동 처리)
        for (String value : normalizedValues) {
            List<AttributeOption> optionsToDelete = attribute.getActiveOptions().stream()
                    .filter(option -> value.equals(option.getValue()))
                    .toList();
            
            for (AttributeOption option : optionsToDelete) {
                // AttributeValue들 먼저 삭제
                clothingAttributeValueRepository.softDeleteAllByOptionId(option.getId());
            }
        }

        // 5. 옵션들 삭제
        attribute.removeOptionsWithCleanup(normalizedValues);
        
        // 6. 저장 및 캐시 무효화
        saveAndEvictAfterCommit(attribute);
    }
    


    private long getAttributeCount(String keywordLike) {
        if (keywordLike != null && !keywordLike.trim().isEmpty()) {
            return clothingAttributeRepository.countByKeyword(keywordLike);
        } else {
            Long cachedCount = attributeCacheService.getAttributeCount();
            return cachedCount != null ? cachedCount : clothingAttributeRepository.countByKeyword(null);
        }
    }

    private void validateCreateRequest(ClothesAttributeDefCreateRequest request) {
        if (request == null || request.name() == null) {
            throw new ClothesException(ErrorCode.INVALID_PARAMETER);
        }
        if (clothingAttributeRepository.findByName(request.name()).isPresent()) {
            throw new ClothesException(ErrorCode.ATTRIBUTE_DUPLICATE);
        }
    }

    private ClothingAttribute persistNewAttribute(ClothesAttributeDefCreateRequest request) {
        ClothingAttribute attribute = ClothingAttribute.createWithOptions(
            request.name(), normalizeValues(request.selectableValues())
        );
        return clothingAttributeRepository.save(attribute);
    }

    private void evictAttributeCachesAfterCommit() {
        registerAfterCommit(() -> {
            attributeCacheService.evictAttributeCount();
            clothListCacheService.evictAttributeListFirstPage();
        });
    }

    private void publishNewAttributeEvent(ClothingAttribute saved) {
        try {
            Long attrId = saved.getId() != null ? saved.getId() : 0L;
            eventPublisher.publishEvent(
                new NewClothAttributeEvent(
                    attrId, saved.getName()
                )
            );
        } catch (Exception e) {
            log.warn("이벤트 발행 실패: {}", e.getMessage());
        }
    }

    private ClothingAttribute getAttributeOrThrow(Long id) {
        return clothingAttributeRepository.findById(id)
            .orElseThrow(() -> new ClothesException(ErrorCode.ATTRIBUTE_NOT_FOUND));
    }

    private void softDeleteAttributeWithValues(ClothingAttribute attribute) {
        List<ClothingAttributeValue> attributeValues = clothingAttributeValueRepository.findByAttributeId(attribute.getId());
        attributeValues.forEach(ClothingAttributeValue::softDelete);
        attribute.softDeleteWithCleanup();
        clothingAttributeRepository.save(attribute);
    }

    private List<String> extractTargetValues(ClothesAttributeDefCreateRequest request) {
        return request.selectableValues() != null ? request.selectableValues() : java.util.List.of();
    }

    private void softDeleteMissingOptions(Long attributeId, List<String> values) {
        // 1단계: 관련된 AttributeValue들 먼저 삭제 (참조 무결성)
        deleteAttributeValuesForMissingOptions(attributeId, values);
        
        // 2단계: AttributeOption들 삭제
        clothingAttributeRepository.softDeleteMissingOptions(attributeId, values);
    }
    

    private void deleteAttributeValuesForMissingOptions(Long attributeId, List<String> values) {
        ClothingAttribute attribute = clothingAttributeRepository.findById(attributeId).orElse(null);
        if (attribute == null || attribute.getActiveOptions().isEmpty()) {
            return;
        }
        
        // 삭제될 옵션들의 ID 수집 (양방향 관계 활용)
        List<Long> optionIdsToDelete = attribute.getActiveOptions().stream()
            .filter(option -> !values.contains(option.getValue()))
            .map(AttributeOption::getId)
            .toList();
            
        // 배치로 한번에 삭제 (성능 최적화)
        if (!optionIdsToDelete.isEmpty()) {
            clothingAttributeValueRepository.deleteAllByOptionIds(optionIdsToDelete);
        }
    }

    private ClothingAttribute reloadAttribute(Long id, ClothingAttribute fallback) {
        return clothingAttributeRepository.findById(id).orElse(fallback);
    }

    private void addMissingOptions(ClothingAttribute refreshed, List<String> values) {
        java.util.Set<String> currentValues = refreshed.getActiveOptions().stream()
            .map(com.stylemycloset.cloth.entity.AttributeOption::getValue)
            .collect(java.util.stream.Collectors.toSet());
        for (String v : values) {
            if (v == null) continue;
            if (!currentValues.contains(v)) {
                // 양방향 관계로 자동으로 컬렉션에 추가됨
                AttributeOption.createOption(refreshed, v);
                currentValues.add(v);
            }
        }
    }

    private void evictAttributeListCacheAfterCommit() {
        registerAfterCommit(clothListCacheService::evictAttributeListFirstPage);
    }

    private void publishAttributeChangedEvent(ClothingAttribute saved) {
        try {
            Long attrId = saved.getId() != null ? saved.getId() : 0L;
            eventPublisher.publishEvent(
                new ClothAttributeChangedEvent(
                    attrId, saved.getName()
                )
            );
        } catch (Exception e) {
            log.warn("이벤트 발행 실패: {}", e.getMessage());
        }
    }

    private AttributeResponseDto saveAndEvictAfterCommit(ClothingAttribute attribute) {
        ClothingAttribute savedAttribute = clothingAttributeRepository.save(attribute);
        evictAttributeListCacheAfterCommit();
        return attributeResponseMapper.toDto(savedAttribute);
    }

    private List<String> normalizeValues(java.util.List<String> rawValues) {
        if (rawValues == null) return List.of();
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String v : rawValues) {
            if (!StringUtils.hasText(v)) continue;
            // 허용: 단일 요소에 콤마로 합쳐 보낸 경우 분리
            String[] parts = v.split(",");
            for (String part : parts) {
                if (StringUtils.hasText(part)) set.add(part.strip());
            }
        }
        return new ArrayList<>(set);
    }

    private void registerAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { task.run(); }
            });
        } else {
            task.run();
        }
    }


} 
package com.stylemycloset.cloth.service;

import com.stylemycloset.cloth.dto.ClothesAttributeDefCreateRequest;
import com.stylemycloset.cloth.dto.ClothesAttributeDefDto;
import com.stylemycloset.cloth.dto.CursorDto;
import com.stylemycloset.cloth.dto.response.PaginatedResponse;
import com.stylemycloset.cloth.dto.response.AttributeResponseDto;
import com.stylemycloset.cloth.dto.SortDirection;
import com.stylemycloset.cloth.entity.ClothingAttribute;
import com.stylemycloset.cloth.entity.ClothingAttributeValue;
import com.stylemycloset.cloth.exception.ClothesException;
import com.stylemycloset.cloth.exception.ClothingErrorCode;
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
        return clothingAttributeRepository.findById(id)
                .map(attribute -> {
                    attribute.updateName(request.name());
                    java.util.List<String> values = normalizeValues(extractTargetValues(request));
                    softDeleteMissingOptions(attribute.getId(), values);
                    ClothingAttribute refreshed = reloadAttribute(attribute.getId(), attribute);
                    addMissingOptions(refreshed, values);
                    ClothingAttribute savedAttribute = clothingAttributeRepository.save(refreshed);
                    evictAttributeListCacheAfterCommit();
                    publishAttributeChangedEvent(savedAttribute);
                    return attributeResponseMapper.toDto(savedAttribute);
                })
                .orElseThrow(() -> new ClothesException(ClothingErrorCode.ATTRIBUTE_NOT_FOUND));
    }

    @Transactional
    public AttributeResponseDto addAttributeOptions(Long id, List<String> optionValues) {
        return clothingAttributeRepository.findById(id)
                .map(attribute -> {
                    attribute.addOptions(normalizeValues(optionValues));
                    return saveAndEvictAfterCommit(attribute);
                })
                .orElseThrow(() -> new ClothesException(ClothingErrorCode.ATTRIBUTE_NOT_FOUND));
    }

    @Transactional
    public AttributeResponseDto removeAttributeOptions(Long id, List<String> optionValues) {
        return clothingAttributeRepository.findById(id)
                .map(attribute -> {
                    attribute.removeOptionsWithCleanup(normalizeValues(optionValues));
                    return saveAndEvictAfterCommit(attribute);
                })
                .orElseThrow(() -> new ClothesException(ClothingErrorCode.ATTRIBUTE_NOT_FOUND));
    }
    


    private long getAttributeCount(String keywordLike) {
        if (keywordLike != null && !keywordLike.trim().isEmpty()) {
            return clothingAttributeRepository.countByKeyword(keywordLike);
        } else {
            Long cachedCount = attributeCacheService.getAttributeCount();
            return cachedCount != null ? cachedCount : clothingAttributeRepository.countByKeyword(null);
        }
    }

    // ===== private helpers =====
    private void validateCreateRequest(ClothesAttributeDefCreateRequest request) {
        if (request == null || request.name() == null) {
            throw new ClothesException(ClothingErrorCode.INVALID_PARAMETER);
        }
        if (clothingAttributeRepository.findByName(request.name()).isPresent()) {
            throw new ClothesException(ClothingErrorCode.ATTRIBUTE_DUPLICATE);
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
        } catch (Exception ignore) {}
    }

    private ClothingAttribute getAttributeOrThrow(Long id) {
        return clothingAttributeRepository.findById(id)
            .orElseThrow(() -> new ClothesException(ClothingErrorCode.ATTRIBUTE_NOT_FOUND));
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
        clothingAttributeRepository.softDeleteMissingOptions(attributeId, values);
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
                com.stylemycloset.cloth.entity.AttributeOption.createOption(refreshed, v);
                currentValues.add(v);
            }
        }
    }

    private void evictAttributeListCacheAfterCommit() {
        registerAfterCommit(() -> clothListCacheService.evictAttributeListFirstPage());
    }

    private void publishAttributeChangedEvent(ClothingAttribute saved) {
        try {
            Long attrId = saved.getId() != null ? saved.getId() : 0L;
            eventPublisher.publishEvent(
                new ClothAttributeChangedEvent(
                    attrId, saved.getName()
                )
            );
        } catch (Exception ignore) {}
    }

    private AttributeResponseDto saveAndEvictAfterCommit(ClothingAttribute attribute) {
        ClothingAttribute savedAttribute = clothingAttributeRepository.save(attribute);
        evictAttributeListCacheAfterCommit();
        return attributeResponseMapper.toDto(savedAttribute);
    }

    private java.util.List<String> normalizeValues(java.util.List<String> rawValues) {
        if (rawValues == null) return java.util.List.of();
        java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<>();
        for (String v : rawValues) {
            if (StringUtils.hasText(v)) {
                set.add(v.strip());
            }
        }
        return new java.util.ArrayList<>(set);
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
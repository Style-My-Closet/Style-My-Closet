package com.stylemycloset.cloth.service;

import com.stylemycloset.cloth.dto.ClothesAttributeDefCreateRequest;
import com.stylemycloset.cloth.dto.ClothesAttributeDefDto;
import com.stylemycloset.cloth.dto.CursorDto;
import com.stylemycloset.cloth.dto.response.AttributeListResponseDto;
import com.stylemycloset.cloth.dto.response.AttributeResponseDto;
import com.stylemycloset.cloth.dto.SortDirection;
import com.stylemycloset.cloth.entity.AttributeOption;
import com.stylemycloset.cloth.entity.ClothingAttribute;
import com.stylemycloset.cloth.entity.ClothingAttributeValue;
import com.stylemycloset.cloth.exception.ClothesException;
import com.stylemycloset.cloth.exception.ClothingErrorCode;
import com.stylemycloset.cloth.repository.ClothingAttributeRepository;
import com.stylemycloset.cloth.repository.ClothingAttributeValueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClothAttributeService {

    private final ClothingAttributeRepository clothingAttributeRepository;
    private final ClothingAttributeValueRepository clothingAttributeValueRepository;
    private final AttributeCacheService attributeCacheService;



    @Transactional(readOnly = true)
    public AttributeListResponseDto findAttributes(CursorDto cursorDto) {
        String keywordLike = cursorDto.keywordLike();
        Long cursor = cursorDto.cursor() != null ? Long.valueOf(cursorDto.cursor()) : null;
        int size = cursorDto.limit() != null ? Integer.parseInt(cursorDto.limit()) : 20;
        String sortBy = cursorDto.sortBy();
        String sortDirection = cursorDto.sortDirection();

        List<ClothingAttribute> attributes = clothingAttributeRepository.findWithCursorPagination(
                keywordLike, cursor, size
        );


        List<ClothesAttributeDefDto> data = attributes != null ? attributes.stream()
                .map(ClothesAttributeDefDto::from)
                .toList() : new ArrayList<>();
        
        long totalCount = getAttributeCount(keywordLike);
        boolean hasNext = attributes != null && attributes.size() == size;
        String nextCursor = hasNext && !attributes.isEmpty() ?
                attributes.getLast().getId().toString() : null;

        return AttributeListResponseDto.builder()
                .data(data)
                .nextCursor(nextCursor)
                .nextIdAfter(nextCursor)
                .hasNext(hasNext)
                .totalCount(totalCount)
                .sortBy(sortBy)
                .sortDirection(sortDirection != null ? SortDirection.valueOf(sortDirection) : SortDirection.ASCENDING)
                .build();
    }

    @Transactional
    public AttributeResponseDto createAttribute(ClothesAttributeDefCreateRequest request) {
        if (clothingAttributeRepository.findByName(request.name()).isPresent()) {
            throw new ClothesException(ClothingErrorCode.ATTRIBUTE_DUPLICATE);
        }
        
        ClothingAttribute attribute = ClothingAttribute.createWithOptions(
                request.name(), 
                request.selectableValues()
        );
        
        ClothingAttribute savedAttribute = clothingAttributeRepository.save(attribute);
        
        // 트랜잭션 커밋 후 캐시 업데이트
        if(TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        attributeCacheService.incrementAttributeCount();
                    } catch (Exception e) {
                        attributeCacheService.evictAttributeCount();
                    }
                }
            });
        }
        
        return AttributeResponseDto.from(savedAttribute);
    }

    @Transactional
    public void deleteAttributeById(Long id) {
        clothingAttributeRepository.findById(id)
                .ifPresent(attribute -> {
                    List<ClothingAttributeValue> attributeValues = clothingAttributeValueRepository.findByAttributeId(id);
                    clothingAttributeValueRepository.deleteAll(attributeValues);
                    
                    attribute.softDelete();
                    
                    clothingAttributeRepository.save(attribute);
                    
                    if(TransactionSynchronizationManager.isActualTransactionActive()) {
                        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                try {
                                    attributeCacheService.decrementAttributeCount();
                                } catch (Exception e) {
                                    log.warn("캐시 업데이트 실패, 캐시 무효화 처리.", e);
                                    attributeCacheService.evictAttributeCount();
                                }
                            }
                        });
                    }
                });
    }

    @Transactional
    public AttributeResponseDto updateAttribute(Long id, ClothesAttributeDefCreateRequest request) {
        return clothingAttributeRepository.findById(id)
                .map(attribute -> {
                    attribute.updateName(request.name());
                    
                    // 기존 옵션들을 모두 제거
                    attribute.updateAttribute(request.name(), request.selectableValues());
                    
                    ClothingAttribute savedAttribute = clothingAttributeRepository.save(attribute);
                    
                    return AttributeResponseDto.from(savedAttribute);
                })
                .orElseThrow(() -> new ClothesException(ClothingErrorCode.ATTRIBUTE_NOT_FOUND));
    }

    @Transactional
    public AttributeResponseDto addAttributeOptions(Long id, List<String> optionValues) {
        return clothingAttributeRepository.findById(id)
                .map(attribute -> {
                    attribute.addOptions(optionValues);
                    
                    ClothingAttribute savedAttribute = clothingAttributeRepository.save(attribute);
                    
                    return AttributeResponseDto.from(savedAttribute);
                })
                .orElseThrow(() -> new ClothesException(ClothingErrorCode.ATTRIBUTE_NOT_FOUND));
    }

    @Transactional
    public AttributeResponseDto removeAttributeOptions(Long id, List<String> optionValues) {
        return clothingAttributeRepository.findById(id)
                .map(attribute -> {
                    attribute.removeOptions(optionValues);
                    
                    ClothingAttribute savedAttribute = clothingAttributeRepository.save(attribute);
                    
                    return AttributeResponseDto.from(savedAttribute);
                })
                .orElseThrow(() -> new ClothesException(ClothingErrorCode.ATTRIBUTE_NOT_FOUND));
    }
    


    private long getAttributeCount(String keywordLike) {
        if (keywordLike != null && !keywordLike.trim().isEmpty()) {
            // 키워드 검색은 캐시 없이 직접 DB 조회
            return clothingAttributeRepository.countByKeyword(keywordLike);
        } else {
            Long cachedCount = attributeCacheService.getAttributeCount();
            if (cachedCount != null) {
                return cachedCount;
            } else {
                // 캐시 미스 시 DB 조회 후 캐시 저장
                long count = clothingAttributeRepository.countByKeyword(null);
                attributeCacheService.setAttributeCount(count);
                return count;
            }
        }
    }


} 
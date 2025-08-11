package com.stylemycloset.cloth.service;

import com.stylemycloset.binarycontent.BinaryContent;
import com.stylemycloset.binarycontent.BinaryContentRepository;
import com.stylemycloset.binarycontent.service.ImageDownloadService;
import com.stylemycloset.cloth.dto.AttributeDto;
import com.stylemycloset.cloth.dto.ClothCreateRequestDto;
import com.stylemycloset.cloth.dto.ClothResponseDto;
import com.stylemycloset.cloth.dto.ClothUpdateRequestDto;
import com.stylemycloset.cloth.dto.CursorDto;
import com.stylemycloset.cloth.dto.SortDirection;
import com.stylemycloset.cloth.dto.SortField;
import com.stylemycloset.cloth.dto.response.ClothItemDto;
import com.stylemycloset.cloth.dto.response.ClothListResponseDto;
import com.stylemycloset.cloth.dto.response.ClothUpdateResponseDto;
import com.stylemycloset.cloth.entity.AttributeOption;
import com.stylemycloset.cloth.entity.Cloth;
import com.stylemycloset.cloth.entity.ClothingAttribute;
import com.stylemycloset.cloth.entity.ClothingAttributeValue;
import com.stylemycloset.cloth.entity.ClothingCategory;
import com.stylemycloset.cloth.entity.ClothingCategoryType;
import com.stylemycloset.cloth.entity.Closet;
import com.stylemycloset.cloth.exception.ClothesException;
import com.stylemycloset.cloth.exception.ClothingErrorCode;
import com.stylemycloset.cloth.repository.AttributeOptionRepository;
import com.stylemycloset.cloth.repository.ClothRepository;
import com.stylemycloset.cloth.repository.ClothingAttributeRepository;
import com.stylemycloset.cloth.repository.ClothingAttributeValueRepository;
import com.stylemycloset.cloth.repository.ClothingCategoryRepository;
import com.stylemycloset.cloth.repository.ClosetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClothService {
    private final ClothRepository clothRepository;
    private final ClothingCategoryRepository categoryRepository;
    private final ClosetRepository closetRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final ClothingAttributeRepository clothingAttributeRepository;
    private final AttributeOptionRepository attributeOptionRepository;
    private final ClothingAttributeValueRepository clothingAttributeValueRepository;

    private final ClothCountCacheService clothCountCacheService;
    private final ClothListCacheService clothListCacheService;
    private final ImageDownloadService imageDownloadService;
    


    @Transactional(readOnly = true)
    public ClothListResponseDto getClothesWithCursor(Long userId, CursorDto cursorDto) {
        validateUserAndCursor(userId, cursorDto);

        // 첫 번째 페이지이고 기본 정렬인 경우 computeIfAbsent 패턴 사용
        if (clothListCacheService.isFirstPage(cursorDto) && clothListCacheService.isDefaultSort(cursorDto)) {
            return clothListCacheService.getClothListFirstPage(
                    userId,
                    () -> computeClothListResponse(userId, cursorDto)
            );
        }

        return computeClothListResponse(userId, cursorDto);
    }

    // 의류 목록
    private ClothListResponseDto computeClothListResponse(Long userId, CursorDto cursorDto) {
        Long idAfter = cursorDto.idAfter();
        int limit = cursorDto.limit();
        boolean isDescending = cursorDto.isDescending();
        boolean hasIdAfter = idAfter != null;

        List<ClothItemDto> clothes = clothRepository.findClothItemDtosWithCursorPagination(
                userId, idAfter, limit + 1, isDescending, hasIdAfter);

        boolean hasNext = clothes.size() > limit;
        List<ClothItemDto> paginatedClothes = hasNext ? clothes.subList(0, limit) : clothes;
        String lastId = !paginatedClothes.isEmpty() ? paginatedClothes.getLast().getId() : null;

        long totalCount = getTotalCount(userId);

        // 커서는 기존 빌더로 나두는 게 나음 조건이 변경이 잦음
        return ClothListResponseDto.builder()
                .data(paginatedClothes)
                .nextCursor(lastId)
                .nextIdAfter(lastId)
                .hasNext(hasNext)
                .totalCount(totalCount)
                .sortBy(cursorDto.sortBy())
                .sortDirection(SortDirection.fromString(cursorDto.sortDirection()))
                .build();
    }


    private long getTotalCount(Long userId) {
        return clothCountCacheService.getUserClothCount(userId);
    }

    @Transactional
    public ClothResponseDto createCloth(ClothCreateRequestDto requestDto, Long userId) {
        Cloth savedCloth = persistCloth(requestDto, userId);

        // 요청에 속성이 있으면 즉시 매핑
        if (requestDto.getAttributes() != null && !requestDto.getAttributes().isEmpty()) {
            savedCloth = saveAttributes(savedCloth, requestDto.getAttributes());
        }
        afterCommit(userId);
        return clothResponse(savedCloth.getId(), savedCloth);
    }

    @Transactional(readOnly = true)
    public ClothResponseDto getClothResponseById(Long clothId) {
        ClothResponseDto dto = getClothResponseDtoById(clothId);
        Cloth cloth = getClothById(clothId);
        java.util.List<AttributeDto> attrs = cloth.getAttributeValues()
                .stream()
                .map(AttributeDto::from)
                .toList();
        dto.setAttributes(attrs);
        return dto;
    }

    @Transactional
    public void upsertAttributesByName(Long clothId, Map<String, List<String>> nameToValues) {
        Cloth cloth = getClothById(clothId);
        // 기존 값 벌크 소프트 삭제 (메모리 컬렉션은 건드리지 않음)
        clothingAttributeValueRepository.softDeleteAllByClothId(clothId);

        if (nameToValues == null || nameToValues.isEmpty()) {
            evictClothListCacheAfterCommit(cloth);
            return;
        }

        for (var entry : nameToValues.entrySet()) {
            String attrName = entry.getKey();
            List<String> rawValues = entry.getValue();
            if (attrName == null) continue;
            List<String> values = normalizeValues(rawValues);
            if (values.isEmpty()) continue;

            ClothingAttribute attribute = getOrCreateAttribute(attrName);
            for (String v : values) {
                AttributeOption option = getOrCreateOption(attribute, v);
                saveAttributeValue(cloth, attribute, option);
            }
        }

        evictClothListCacheAfterCommit(cloth);
    }

    @Transactional
    public ClothUpdateResponseDto updateCloth(Long clothId, ClothUpdateRequestDto requestDto, MultipartFile image) {
        Cloth cloth = getClothById(clothId);
        updateCloth(cloth, requestDto, image);
        Cloth savedCloth = clothRepository.save(cloth);
        evictClothListCacheAfterCommit(cloth);
        return ClothUpdateResponseDto.from(savedCloth);
    }

    @Transactional
    public void deleteCloth(Long clothId) {
        Cloth cloth = getClothById(clothId);
        Long userId = cloth.getCloset().getUser().getId();
        clothingAttributeValueRepository.softDeleteAllByClothId(clothId);
        Cloth managedCloth = clothRepository.findById(clothId)
                .orElseThrow(() -> new ClothesException(ClothingErrorCode.CLOTH_NOT_FOUND));
        managedCloth.softDeleteWithCleanup();
        clothRepository.save(managedCloth);
        afterCommit(userId);
    }


    // 커서 값 검증
    private void validateUserAndCursor(Long userId, CursorDto cursorDto) {
        if (userId == null || userId <= 0) {
            throw new ClothesException(ClothingErrorCode.UNAUTHORIZED_ACCESS);
        }

        if (cursorDto.limit() <= 0 || cursorDto.limit() > 100) {
            throw new ClothesException(ClothingErrorCode.INVALID_PARAMETER);
        }

        // 정렬 조건 검증
        if (cursorDto.sortBy() != null && !isValidSortBy(cursorDto.sortBy())) {
            throw new ClothesException(ClothingErrorCode.INVALID_PARAMETER);
        }
    }

    // 정렬 조건 검증
    private boolean isValidSortBy(String sortBy) {
        if (sortBy == null) return true;

        try {
            SortField.fromString(sortBy);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
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

    private ClothingCategory getOrCreateCategoryByType(ClothingCategoryType type) {
        return categoryRepository.findByName(type)
                .orElseGet(() -> categoryRepository.save(new ClothingCategory(type)));
    }

    private BinaryContent getBinaryContentById(UUID binaryContentId) {
        return binaryContentRepository.findById(binaryContentId)
                .orElseThrow(RuntimeException::new);
    }

    private Cloth getClothById(Long clothId) {
        return clothRepository.findById(clothId)
                .orElseThrow(() -> new ClothesException(ClothingErrorCode.CLOTH_NOT_FOUND));
    }


    private ClothResponseDto getClothResponseDtoById(Long clothId) {
        return clothRepository.findClothResponseDtoById(clothId)
                .orElseThrow(() -> new ClothesException(ClothingErrorCode.CLOTH_NOT_FOUND));
    }

    private Cloth saveCloth(ClothCreateRequestDto requestDto, Closet closet,
                            ClothingCategory category, BinaryContent binaryContent) {
        Cloth cloth = Cloth.createCloth(requestDto.getName(), closet, category, binaryContent);
        return clothRepository.save(cloth);
    }

    private void updateCloth(Cloth cloth, ClothUpdateRequestDto requestDto, MultipartFile image) {
        if (requestDto.getName() != null) {
            cloth.updateName(requestDto.getName());
        }

        // 속성값 업데이트 처리
        if (requestDto.getAttributes() != null && !requestDto.getAttributes().isEmpty()) {
            clothingAttributeValueRepository.softDeleteAllByClothId(cloth.getId());
            cloth.getAttributeValues().clear();
            for (ClothUpdateRequestDto.AttributeRequestDto attrDto : requestDto.getAttributes()) {
                ClothingAttribute attribute = clothingAttributeRepository.findById(attrDto.getDefinitionId())
                        .orElseThrow(() -> new ClothesException(ClothingErrorCode.ATTRIBUTE_NOT_FOUND));
                AttributeOption option = attributeOptionRepository.findById(attrDto.getOptionId())
                        .orElseThrow(() -> new ClothesException(ClothingErrorCode.INVALID_ATTRIBUTE));
                cloth.addAttributeValue(attribute, option);
            }
        }

        // 이미지 교체 처리
        if (image != null) {
            if (!image.isEmpty()) {
                try {
                    BinaryContent oldBinaryContent = cloth.getBinaryContent();
                    BinaryContent newBinaryContent = imageDownloadService.updateImage(oldBinaryContent, image);
                    cloth.updateBinaryContent(newBinaryContent);
                } catch (Exception e) {
                    throw new ClothesException(ClothingErrorCode.IMAGE_UPLOAD_FAILED);
                }
            }
        }
    }

  
    private List<String> normalizeValues(List<String> rawValues) {
        if (rawValues == null) return List.of();
        HashSet <String> set = new LinkedHashSet<>(rawValues);
        for (String v : rawValues) {
            if (StringUtils.hasText(v)) {
                set.add(v.strip());
            }
        }
        return new java.util.ArrayList<>(set);
    }

    private ClothingAttribute getOrCreateAttribute(String attrName) {
        return clothingAttributeRepository.findByName(attrName)
            .orElseGet(() -> clothingAttributeRepository.save(ClothingAttribute.createWithOptions(attrName, java.util.List.of())));
    }

    private AttributeOption getOrCreateOption(ClothingAttribute attribute, String value) {
        return attributeOptionRepository.findByAttributeAndValue(attribute, value)
            .orElseGet(() -> attributeOptionRepository.save(
                AttributeOption.builder().attribute(attribute).value(value).build()
            ));
    }

    private void saveAttributeValue(Cloth cloth, ClothingAttribute attribute, AttributeOption option) {
        clothingAttributeValueRepository.save(
            ClothingAttributeValue.builder()
                .cloth(cloth)
                .attribute(attribute)
                .option(option)
                .build()
        );
    }

    private void evictClothListCacheAfterCommit(Cloth cloth) {
        try {
            Long userId = cloth.getCloset() != null && cloth.getCloset().getUser() != null ? cloth.getCloset().getUser().getId() : null;
            if (userId == null) return;
            registerAfterCommit(() -> clothListCacheService.evictClothListFirstPage(userId));
        } catch (Exception ignore) {}
    }

    private Cloth persistCloth(ClothCreateRequestDto requestDto, Long userId) {
        Closet closet = getClosetByUserId(userId);
        ClothingCategory category = requestDto.getCategoryId() != null ?
            getCategoryById(requestDto.getCategoryId()) : getOrCreateCategoryByType(ClothingCategoryType.from(requestDto.getType()));
        BinaryContent binaryContent = requestDto.getBinaryContentId() != null ?
            getBinaryContentById(requestDto.getBinaryContentId()) : null;
        return saveCloth(requestDto, closet, category, binaryContent);
    }

    private Cloth saveAttributes(Cloth cloth, java.util.List<ClothCreateRequestDto.AttributeRequestDto> attributes) {
        for (ClothCreateRequestDto.AttributeRequestDto attrDto : attributes) {
            ClothingAttribute attribute = clothingAttributeRepository.findById(attrDto.getDefinitionId())
                .orElseThrow(() -> new ClothesException(ClothingErrorCode.ATTRIBUTE_NOT_FOUND));
            AttributeOption option = attributeOptionRepository.findById(attrDto.getOptionId())
                .orElseThrow(() -> new ClothesException(ClothingErrorCode.INVALID_ATTRIBUTE));
            cloth.addAttributeValue(attribute, option);
        }
        return clothRepository.save(cloth);
    }

    private void afterCommit(Long userId) {
        if (userId == null) return;
        registerAfterCommit(() -> {
            clothCountCacheService.evictUserClothCount(userId);
            clothListCacheService.evictClothListFirstPage(userId);
        });
    }

    private ClothResponseDto clothResponse(Long clothId, Cloth savedCloth) {
        ClothResponseDto dto = getClothResponseDtoById(clothId);
        java.util.List<AttributeDto> attrs = savedCloth.getAttributeValues()
            .stream()
            .map(AttributeDto::from)
            .toList();
        dto.setAttributes(attrs);
        return dto;
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


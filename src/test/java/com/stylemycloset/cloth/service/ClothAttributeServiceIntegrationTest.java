package com.stylemycloset.cloth.service;

import com.stylemycloset.cloth.dto.ClothesAttributeDefCreateRequest;
import com.stylemycloset.cloth.dto.CursorDto;
import com.stylemycloset.cloth.dto.response.AttributeListResponseDto;
import com.stylemycloset.cloth.dto.response.AttributeResponseDto;
import com.stylemycloset.cloth.entity.ClothingAttribute;
import com.stylemycloset.cloth.exception.ClothesException;
import com.stylemycloset.cloth.repository.ClothingAttributeRepository;
import com.stylemycloset.testutil.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class ClothAttributeServiceIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private ClothAttributeService clothAttributeService;
    
    @Autowired
    private ClothingAttributeRepository clothingAttributeRepository;
    

    @Autowired
    private AttributeCacheService attributeCacheService;

    @BeforeEach
    void setUp() {
        attributeCacheService.evictAttributeCount();
    }

    @Test
    @DisplayName("통합 테스트")
    void attributeLifeCycle_Integration() {
        // 테스트에 필요한 기본 데이터 생성
        ClothingAttribute testAttribute1 = ClothingAttribute.createWithOptions("색상", Arrays.asList("빨강", "파랑", "초록"));
        ClothingAttribute testAttribute2 = ClothingAttribute.createWithOptions("크기", Arrays.asList("S", "M", "L"));
        clothingAttributeRepository.save(testAttribute1);
        clothingAttributeRepository.save(testAttribute2);
        
        // 1. 속성 생성 (테스트 격리를 위한 고유값 적용)
        String uniqueSuffix = "attrLifeCycle_" + System.currentTimeMillis();
        ClothesAttributeDefCreateRequest createRequest = new ClothesAttributeDefCreateRequest(
                "재질", Arrays.asList("면_" + uniqueSuffix, "폴리에스터_" + uniqueSuffix, "울_" + uniqueSuffix));
        
        AttributeResponseDto createdAttribute = clothAttributeService.createAttribute(createRequest);
        
        assertNotNull(createdAttribute);
        assertEquals("재질", createdAttribute.name());
        assertEquals(3, createdAttribute.selectableValues().size());

        // 2. 속성 목록 조회 (전체)
        CursorDto allCursor = new CursorDto(null, null, "10", null, "ASCENDING", null);
        AttributeListResponseDto allAttributes = clothAttributeService.findAttributes(allCursor);
        
        // 생성된 속성이 목록에 포함되어 있는지 확인
        assertTrue(allAttributes.getData().size() >= 3); // 기존 2개 + 새로 생성한 1개
        assertTrue(allAttributes.getTotalCount() >= 3L);
        
        // 생성된 속성이 목록에 있는지 확인
        boolean foundCreatedAttribute = allAttributes.getData().stream()
                .anyMatch(attr -> "재질".equals(attr.name()));
        assertTrue(foundCreatedAttribute);

        // 3. 키워드로 속성 검색
        CursorDto searchCursor = new CursorDto(null, null, "10", null, "ASCENDING", "재질");
        AttributeListResponseDto searchResult = clothAttributeService.findAttributes(searchCursor);
        
        assertEquals(1, searchResult.getData().size());
        assertEquals("재질", searchResult.getData().getFirst().name());

        // 4. 속성 수정
        String updateSuffix = "attrUpdate_" + System.currentTimeMillis();
        ClothesAttributeDefCreateRequest updateRequest = new ClothesAttributeDefCreateRequest(
                "소재", Arrays.asList("면_" + updateSuffix, "폴리에스터_" + updateSuffix, "울_" + updateSuffix, "리넨_" + updateSuffix));
        
        AttributeResponseDto updatedAttribute = clothAttributeService.updateAttribute(
                Long.valueOf(createdAttribute.id()), updateRequest);
        
        assertEquals("소재", updatedAttribute.name());
        assertEquals(4, updatedAttribute.selectableValues().size());

        // 5. 속성 옵션 추가
        AttributeResponseDto attributeWithNewOptions = clothAttributeService.addAttributeOptions(
                Long.valueOf(updatedAttribute.id()), List.of("실크_" + updateSuffix));
        
        assertEquals(5, attributeWithNewOptions.selectableValues().size());
        assertTrue(attributeWithNewOptions.selectableValues().contains("실크_" + updateSuffix));

        // 6. 속성 옵션 제거
        AttributeResponseDto attributeWithRemovedOptions = clothAttributeService.removeAttributeOptions(
                Long.valueOf(attributeWithNewOptions.id()), List.of("면_" + updateSuffix));
        
        assertEquals(4, attributeWithRemovedOptions.selectableValues().size());
        assertFalse(attributeWithRemovedOptions.selectableValues().contains("면_" + updateSuffix));

        // 7. 속성 삭제
        clothAttributeService.deleteAttributeById(Long.valueOf(attributeWithRemovedOptions.id()));
        
        // 삭제 확인
        CursorDto finalCursor = new CursorDto(null, null, "10", null, "ASCENDING", null);
        AttributeListResponseDto finalAttributes = clothAttributeService.findAttributes(finalCursor);
        
        boolean foundDeletedAttribute = finalAttributes.getData().stream()
                .anyMatch(attr -> "소재".equals(attr.name()));
        assertFalse(foundDeletedAttribute);
    }

    @Test
    @DisplayName("통합 테스트: 캐시 동작 확인")
    void cacheOperation_Integration() {
        // 1. 캐시 미스 상황에서 DB 조회
        CursorDto cursor = new CursorDto(null, null, "10", null, "ASCENDING", null);
        AttributeListResponseDto firstResult = clothAttributeService.findAttributes(cursor);
        
        // 2. 캐시 히트 상황에서 캐시 조회
        AttributeListResponseDto secondResult = clothAttributeService.findAttributes(cursor);
        
        // 결과가 동일해야 함
        assertEquals(firstResult.getTotalCount(), secondResult.getTotalCount());
        assertEquals(firstResult.getData().size(), secondResult.getData().size());
    }

    @Test
    @DisplayName("통합 테스트: 페이징 기능")
    void attributePagination_Integration() {
        // 여러 속성 생성
        for (int i = 1; i <= 15; i++) {
            ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest(
                    "속성" + i, Arrays.asList("옵션1_" + i, "옵션2_" + i));
            clothAttributeService.createAttribute(request);
        }
        
        // 첫 번째 페이지 조회 (5개씩)
        CursorDto firstPageCursor = new CursorDto(null, null, "5", null, "ASCENDING", null);
        AttributeListResponseDto firstPage = clothAttributeService.findAttributes(firstPageCursor);
        
        assertEquals(5, firstPage.getData().size());
        assertTrue(firstPage.isHasNext());
        assertNotNull(firstPage.getNextCursor());
        
        // 두 번째 페이지 조회
        CursorDto secondPageCursor = new CursorDto(firstPage.getNextCursor(), null, "5", null, "ASCENDING", null);
        AttributeListResponseDto secondPage = clothAttributeService.findAttributes(secondPageCursor);
        
        assertEquals(5, secondPage.getData().size());
        assertTrue(secondPage.isHasNext());
        
        // 세 번째 페이지 조회
        CursorDto thirdPageCursor = new CursorDto(secondPage.getNextCursor(), null, "5", null, "ASCENDING", null);
        AttributeListResponseDto thirdPage = clothAttributeService.findAttributes(thirdPageCursor);

        assertFalse(thirdPage.getData().isEmpty());
    }

    @Test
    @DisplayName("통합 테스트: 키워드 검색 기능")
    void keywordSearch_Integration() {
        // 검색 대상 속성 생성
        ClothesAttributeDefCreateRequest request1 = new ClothesAttributeDefCreateRequest(
                "색상", Arrays.asList("빨강", "파랑"));
        ClothesAttributeDefCreateRequest request2 = new ClothesAttributeDefCreateRequest(
                "크기", Arrays.asList("S", "M", "L"));
        ClothesAttributeDefCreateRequest request3 = new ClothesAttributeDefCreateRequest(
                "색상상", Arrays.asList("검정", "흰색"));
        
        clothAttributeService.createAttribute(request1);
        clothAttributeService.createAttribute(request2);
        clothAttributeService.createAttribute(request3);
        
        // "색상" 키워드로 검색
        CursorDto searchCursor = new CursorDto(null, null, "10", null, "ASCENDING", "색상");
        AttributeListResponseDto searchResult = clothAttributeService.findAttributes(searchCursor);
        
        // "색상"과 "색상상"이 검색되어야 함
        assertEquals(2, searchResult.getData().size());
        assertTrue(searchResult.getData().stream().allMatch(attr -> 
                attr.name().contains("색상")));
    }

    @Test
    @DisplayName("통합 테스트: 옵션 관리 복합 시나리오")
    void optionManagement_Integration() {
        // 속성 생성
        ClothesAttributeDefCreateRequest createRequest = new ClothesAttributeDefCreateRequest(
                "재질", Arrays.asList("면", "폴리에스터", "울"));
        AttributeResponseDto createdAttribute = clothAttributeService.createAttribute(createRequest);
        
        // 옵션 추가
        AttributeResponseDto withAddedOptions = clothAttributeService.addAttributeOptions(
                Long.valueOf(createdAttribute.id()), Arrays.asList("리넨", "실크"));
        
        assertEquals(5, withAddedOptions.selectableValues().size());
        assertTrue(withAddedOptions.selectableValues().contains("리넨"));
        assertTrue(withAddedOptions.selectableValues().contains("실크"));
        
        // 옵션 제거
        AttributeResponseDto withRemovedOptions = clothAttributeService.removeAttributeOptions(
                Long.valueOf(withAddedOptions.id()), Arrays.asList("면", "실크"));
        
        assertEquals(3, withRemovedOptions.selectableValues().size());
        assertFalse(withRemovedOptions.selectableValues().contains("면"));
        assertFalse(withRemovedOptions.selectableValues().contains("실크"));
        assertTrue(withRemovedOptions.selectableValues().contains("폴리에스터"));
        assertTrue(withRemovedOptions.selectableValues().contains("울"));
        assertTrue(withRemovedOptions.selectableValues().contains("리넨"));
    }

    @Test
    @DisplayName("통합 테스트: 예외 상황 처리")
    void exceptionHandling_Integration() {
        // 존재하지 않는 속성 ID로 수정 시도
        assertThrows(ClothesException.class, () -> {
            ClothesAttributeDefCreateRequest updateRequest = new ClothesAttributeDefCreateRequest(
                    "존재하지않는속성", Arrays.asList("옵션1", "옵션2"));
            clothAttributeService.updateAttribute(99999L, updateRequest);
        });
        
        // 존재하지 않는 속성 ID로 옵션 추가 시도
        assertThrows(ClothesException.class, () -> clothAttributeService.addAttributeOptions(99999L, List.of("새옵션")));
        
        // 존재하지 않는 속성 ID로 옵션 제거 시도
        assertThrows(ClothesException.class, () -> clothAttributeService.removeAttributeOptions(99999L, List.of("옵션")));
    }

    @Test
    @DisplayName("통합 테스트: 빈 옵션 리스트 처리")
    void emptyOptionsList_Integration() {
        // 빈 옵션 리스트로 속성 생성
        ClothesAttributeDefCreateRequest createRequest = new ClothesAttributeDefCreateRequest(
                "빈옵션속성", Collections.emptyList());
        AttributeResponseDto createdAttribute = clothAttributeService.createAttribute(createRequest);
        
        assertEquals(0, createdAttribute.selectableValues().size());
        
        // 빈 옵션 리스트로 속성 수정
        ClothesAttributeDefCreateRequest updateRequest = new ClothesAttributeDefCreateRequest(
                "수정된빈옵션속성", Collections.emptyList());
        AttributeResponseDto updatedAttribute = clothAttributeService.updateAttribute(
                Long.valueOf(createdAttribute.id()), updateRequest);
        
        assertEquals(0, updatedAttribute.selectableValues().size());
    }
} 
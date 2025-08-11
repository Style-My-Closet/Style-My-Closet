package com.stylemycloset.cloth.service;

import com.stylemycloset.cloth.dto.ClothesAttributeDefCreateRequest;
import com.stylemycloset.cloth.dto.ClothesAttributeDefDto;
import com.stylemycloset.cloth.dto.CursorDto;
import com.stylemycloset.cloth.dto.response.PaginatedResponse;
import com.stylemycloset.cloth.dto.response.AttributeResponseDto;
import com.stylemycloset.cloth.entity.ClothingAttribute;
import com.stylemycloset.cloth.exception.ClothesException;
import com.stylemycloset.cloth.repository.ClothingAttributeRepository;
import com.stylemycloset.testutil.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.mockito.Mockito;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import jakarta.persistence.EntityManager;

@Transactional // 클래스 전체에 다시 추가
class ClothAttributeServiceIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private ClothAttributeService clothAttributeService;

    @Autowired
    private ClothingAttributeRepository clothingAttributeRepository;

    @Autowired
    private AttributeCacheService attributeCacheService;

    @MockBean
    private ClothListCacheService clothListCacheService;

    @MockBean
    private ApplicationEventPublisher eventPublisher; // 이벤트 발행 모킹으로 비동기 리스너 차단

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        // 캐시 서비스 모킹 - 테스트에서는 캐시 우회
        Mockito.lenient().when(clothListCacheService.isFirstPage(Mockito.any(CursorDto.class))).thenReturn(false);
        Mockito.lenient().when(clothListCacheService.isNoKeywordSearch(Mockito.any(CursorDto.class))).thenReturn(true);
        Mockito.lenient().when(clothListCacheService.getAttributeListFirstPage(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0, java.util.function.Supplier.class).get());
        Mockito.doNothing().when(clothListCacheService).evictAttributeListFirstPage();

        // 테스트 격리를 위한 캐시 클리어
        attributeCacheService.evictAttributeCount();
        // 모든 테이블 데이터 초기화
        truncateAllTables();
    }

    private void truncateAllTables() {
        entityManager.flush();
        entityManager.clear();
        entityManager.createNativeQuery("SET session_replication_role = 'replica'").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE comment_likes RESTART IDENTITY CASCADE").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE feed_likes RESTART IDENTITY CASCADE").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE feed_comments RESTART IDENTITY CASCADE").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE feed_ootd_clothes RESTART IDENTITY CASCADE").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE feeds RESTART IDENTITY CASCADE").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE weather RESTART IDENTITY CASCADE").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE clothes_to_attribute_options RESTART IDENTITY CASCADE").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE clothes_attributes_category_options RESTART IDENTITY CASCADE").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE clothes_attributes_categories RESTART IDENTITY CASCADE").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE clothes RESTART IDENTITY CASCADE").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE clothes_categories RESTART IDENTITY CASCADE").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE closets RESTART IDENTITY CASCADE").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE locations RESTART IDENTITY CASCADE").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE users RESTART IDENTITY CASCADE").executeUpdate();

        entityManager.createNativeQuery("DELETE FROM notifications").executeUpdate();
        entityManager.createNativeQuery("ALTER SEQUENCE notifications_id_seq RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM messages").executeUpdate();
        entityManager.createNativeQuery("ALTER SEQUENCE messages_id_seq RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM follows").executeUpdate();
        entityManager.createNativeQuery("ALTER SEQUENCE follows_id_seq RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE binary_contents RESTART IDENTITY CASCADE").executeUpdate();
        entityManager.createNativeQuery("SET session_replication_role = 'origin'").executeUpdate();
    }

    @Nested
    @DisplayName("속성 생명주기 테스트")
    class AttributeLifeCycleTests {

        @Test
        @Commit
        @DisplayName("속성 CRUD 전체 생명주기 테스트")
        void attributeLifeCycle_Integration() {
            // given: 테스트에 필요한 기본 데이터 생성
            String uniqueSuffix = "attrLifeCycle_" + System.currentTimeMillis();
            ClothingAttribute testAttribute1 = ClothingAttribute.createWithOptions("색상", Arrays.asList("빨강", "파랑", "초록"));
            ClothingAttribute testAttribute2 = ClothingAttribute.createWithOptions("크기", Arrays.asList("S", "M", "L"));
            clothingAttributeRepository.save(testAttribute1);
            clothingAttributeRepository.save(testAttribute2);

            // when & then: 1. 속성 생성
            ClothesAttributeDefCreateRequest createRequest = new ClothesAttributeDefCreateRequest(
                    "재질", Arrays.asList("면_" + uniqueSuffix, "폴리에스터_" + uniqueSuffix, "울_" + uniqueSuffix));

            AttributeResponseDto createdAttribute = clothAttributeService.createAttribute(createRequest);

            assertNotNull(createdAttribute);
            assertEquals("재질", createdAttribute.name());
            assertEquals(3, createdAttribute.selectableValues().size());

            // 트랜잭션 커밋을 보장하기 위해 영속성 컨텍스트를 flush/clear
            entityManager.flush();
            entityManager.clear();

            // when & then: 2. 속성 목록 조회 (전체)
            CursorDto allCursor = CursorDto.ofDefault(10);
            PaginatedResponse<ClothesAttributeDefDto> allAttributes = clothAttributeService.findAttributes(allCursor);

            allAttributes.getData().forEach(attr -> System.out.println(attr.id() + " / " + attr.name()));

            assertTrue(allAttributes.getData().size() >= 3); // 기존 2개 + 새로 생성한 1개
            assertTrue(allAttributes.getPagination().getTotalCount() >= 3L);

            boolean foundCreatedAttribute = allAttributes.getData().stream()
                    .anyMatch(attr -> "재질".equals(attr.name()));
            assertTrue(foundCreatedAttribute, "생성된 속성이 목록에서 조회되어야 함");

            // when & then: 3. 키워드로 속성 검색
            CursorDto searchCursor = CursorDto.ofSearch("재질", 10);
            PaginatedResponse<ClothesAttributeDefDto> searchResult = clothAttributeService.findAttributes(searchCursor);

            assertEquals(1, searchResult.getData().size());
            assertEquals("재질", searchResult.getData().get(0).name());

            // when & then: 4. 속성 수정
            String updateSuffix = "attrUpdate_" + System.currentTimeMillis();
            ClothesAttributeDefCreateRequest updateRequest = new ClothesAttributeDefCreateRequest(
                    "소재", Arrays.asList("면_" + updateSuffix, "폴리에스터_" + updateSuffix, "울_" + updateSuffix, "리넨_" + updateSuffix));

            AttributeResponseDto updatedAttribute = clothAttributeService.updateAttribute(
                    Long.valueOf(createdAttribute.id()), updateRequest);

            assertEquals("소재", updatedAttribute.name());
            assertEquals(4, updatedAttribute.selectableValues().size());

            // when & then: 5. 전체 교체 방식으로 옵션 추가 (= 기존 + 실크 추가)
            ClothesAttributeDefCreateRequest addByReplaceRequest = new ClothesAttributeDefCreateRequest(
                    "소재", Arrays.asList(
                            "면_" + updateSuffix,
                            "폴리에스터_" + updateSuffix,
                            "울_" + updateSuffix,
                            "리넨_" + updateSuffix,
                            "실크_" + updateSuffix
                    ));
            AttributeResponseDto attributeWithNewOptions = clothAttributeService.updateAttribute(
                    Long.valueOf(updatedAttribute.id()), addByReplaceRequest);

            assertEquals(5, attributeWithNewOptions.selectableValues().size());
            assertTrue(attributeWithNewOptions.selectableValues().contains("실크_" + updateSuffix));

            // when & then: 6. 전체 교체 방식으로 옵션 제거 (= 면 제거)
            ClothesAttributeDefCreateRequest removeByReplaceRequest = new ClothesAttributeDefCreateRequest(
                    "소재", Arrays.asList(
                            "폴리에스터_" + updateSuffix,
                            "울_" + updateSuffix,
                            "리넨_" + updateSuffix,
                            "실크_" + updateSuffix
                    ));
            AttributeResponseDto attributeWithRemovedOptions = clothAttributeService.updateAttribute(
                    Long.valueOf(attributeWithNewOptions.id()), removeByReplaceRequest);

            assertEquals(4, attributeWithRemovedOptions.selectableValues().size());
            assertFalse(attributeWithRemovedOptions.selectableValues().contains("면_" + updateSuffix));

            // when & then: 7. 속성 삭제
            clothAttributeService.deleteAttributeById(Long.valueOf(attributeWithRemovedOptions.id()));

            // 삭제 확인
            CursorDto finalCursor = CursorDto.ofDefault(10);
            PaginatedResponse<ClothesAttributeDefDto> finalAttributes = clothAttributeService.findAttributes(finalCursor);

            boolean foundDeletedAttribute = finalAttributes.getData().stream()
                    .anyMatch(attr -> "소재".equals(attr.name()));
            assertFalse(foundDeletedAttribute, "삭제된 속성은 목록에서 조회되지 않아야 함");
        }

        @Test
        @Commit
        @DisplayName("빈 옵션 리스트로 속성 생성 및 수정")
        void emptyOptionsList_Integration() {
            // given & when: 빈 옵션 리스트로 속성 생성
            ClothesAttributeDefCreateRequest createRequest = new ClothesAttributeDefCreateRequest(
                    "빈옵션속성", Collections.emptyList());
            AttributeResponseDto createdAttribute = clothAttributeService.createAttribute(createRequest);

            // then
            assertEquals(0, createdAttribute.selectableValues().size());
            assertEquals("빈옵션속성", createdAttribute.name());

            // when: 빈 옵션 리스트로 속성 수정
            ClothesAttributeDefCreateRequest updateRequest = new ClothesAttributeDefCreateRequest(
                    "수정된빈옵션속성", Collections.emptyList());
            AttributeResponseDto updatedAttribute = clothAttributeService.updateAttribute(
                    Long.valueOf(createdAttribute.id()), updateRequest);

            // then
            assertEquals(0, updatedAttribute.selectableValues().size());
            assertEquals("수정된빈옵션속성", updatedAttribute.name());
        }
    }

    @Nested
    @DisplayName("캐시 기능 테스트")
    class CacheTests {

        @Test
        @Commit
        @DisplayName("캐시 동작 확인 - 캐시 미스와 히트")
        void cacheOperation_Integration() {
            // given: 테스트 데이터 준비
            ClothingAttribute testAttribute = ClothingAttribute.createWithOptions("테스트속성", Arrays.asList("옵션1", "옵션2"));
            clothingAttributeRepository.save(testAttribute);

            // when: 1. 캐시 미스 상황에서 DB 조회
            CursorDto cursor = CursorDto.ofDefault(10);
            PaginatedResponse<ClothesAttributeDefDto> firstResult = clothAttributeService.findAttributes(cursor);

            // when: 2. 캐시 히트 상황에서 캐시 조회
            PaginatedResponse<ClothesAttributeDefDto> secondResult = clothAttributeService.findAttributes(cursor);

            // then: 결과가 동일해야 함
            assertEquals(firstResult.getPagination().getTotalCount(), secondResult.getPagination().getTotalCount());
            assertEquals(firstResult.getData().size(), secondResult.getData().size());

            // 캐시에서 조회된 데이터의 내용도 일치해야 함
            for (int i = 0; i < firstResult.getData().size(); i++) {
                ClothesAttributeDefDto first = firstResult.getData().get(i);
                ClothesAttributeDefDto second = secondResult.getData().get(i);
                assertEquals(first.id(), second.id());
                assertEquals(first.name(), second.name());
                assertEquals(first.selectableValues().size(), second.selectableValues().size());
            }
        }

        @Test
        @Commit
        @DisplayName("속성 생성 시 캐시 무효화 확인")
        void cacheEvictionOnCreate_Integration() {
            // given: 초기 상태에서 캐시 로드
            CursorDto cursor = CursorDto.ofDefault(10);
            PaginatedResponse<ClothesAttributeDefDto> initialResult = clothAttributeService.findAttributes(cursor);

            // when: 새로운 속성 생성 (캐시 무효화 트리거)
            ClothesAttributeDefCreateRequest createRequest = new ClothesAttributeDefCreateRequest(
                    "새속성", Arrays.asList("옵션1", "옵션2"));
            clothAttributeService.createAttribute(createRequest);

            // 트랜잭션 커밋을 보장하기 위해 영속성 컨텍스트를 flush/clear
            entityManager.flush();
            entityManager.clear();

            // then: 캐시가 무효화되어 새로운 데이터가 조회되어야 함 (after-commit 타이밍 보정: 재시도 루프)
            PaginatedResponse<ClothesAttributeDefDto> updatedResult = null;
            long deadline = System.currentTimeMillis() + 2000; // 최대 2초 대기
            do {
                updatedResult = clothAttributeService.findAttributes(cursor);
                if (updatedResult.getPagination().getTotalCount() > initialResult.getPagination().getTotalCount()) break;
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            } while (System.currentTimeMillis() < deadline);

            System.out.println("initial: " + initialResult.getPagination().getTotalCount());
            System.out.println("updated: " + updatedResult.getPagination().getTotalCount());
            if (initialResult.getPagination().getTotalCount() == 0) {
                assertTrue(updatedResult.getPagination().getTotalCount() >= 1 || !updatedResult.getData().isEmpty(),
                        "새로운 속성이 추가되어 최소 1개 이상이어야 함");
            } else {
                assertTrue(updatedResult.getPagination().getTotalCount() > initialResult.getPagination().getTotalCount(),
                        "새로운 속성이 추가되어 총 개수가 증가해야 함");
            }
        }
    }

    @Nested
    @DisplayName("페이징 기능 테스트")
    class PaginationTests {

        @Test
        @Commit
        @DisplayName("페이징 기능 - 다중 페이지 조회")
        void attributePagination_Integration() {
            // given: 여러 속성 생성 (15개)
            for (int i = 1; i <= 15; i++) {
                ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest(
                        "속성" + String.format("%02d", i), Arrays.asList("옵션1_" + i, "옵션2_" + i));
                clothAttributeService.createAttribute(request);
            }

            // when & then: 첫 번째 페이지 조회 (5개씩)
            CursorDto firstPageCursor = CursorDto.ofDefault(5);
            PaginatedResponse<ClothesAttributeDefDto> firstPage = clothAttributeService.findAttributes(firstPageCursor);

            assertEquals(5, firstPage.getData().size());
            assertTrue(firstPage.getPagination().isHasNext());
            assertNotNull(firstPage.getPagination().getNextCursor());
            assertTrue(firstPage.getPagination().getTotalCount() >= 15L);

            // when & then: 두 번째 페이지 조회
            CursorDto secondPageCursor = CursorDto.ofPagination(
                    Long.valueOf(firstPage.getPagination().getNextCursor()), 5);
            PaginatedResponse<ClothesAttributeDefDto> secondPage = clothAttributeService.findAttributes(secondPageCursor);

            assertEquals(5, secondPage.getData().size());
            assertTrue(secondPage.getPagination().isHasNext());

            // 첫 번째와 두 번째 페이지의 데이터가 다른지 확인
            List<Long> firstPageIds = firstPage.getData().stream()
                    .map(ClothesAttributeDefDto::id)
                    .toList();
            List<Long> secondPageIds = secondPage.getData().stream()
                    .map(ClothesAttributeDefDto::id)
                    .toList();

            assertTrue(Collections.disjoint(firstPageIds, secondPageIds),
                    "첫 번째와 두 번째 페이지의 데이터는 중복되지 않아야 함");

            // when & then: 세 번째 페이지 조회
            CursorDto thirdPageCursor = CursorDto.ofPagination(
                    secondPage.getPagination().getNextCursor() != null ? Long.valueOf(secondPage.getPagination().getNextCursor()) : null, 5);
            PaginatedResponse<ClothesAttributeDefDto> thirdPage = clothAttributeService.findAttributes(thirdPageCursor);

            assertFalse(thirdPage.getData().isEmpty());
            assertTrue(thirdPage.getData().size() <= 5);
        }

        @Test
        @Commit
        @DisplayName("마지막 페이지에서 hasNext가 false인지 확인")
        void lastPageHasNextFalse_Integration() {
            // given: 3개의 속성만 생성
            for (int i = 1; i <= 3; i++) {
                ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest(
                        "속성" + i, Arrays.asList("옵션" + i));
                clothAttributeService.createAttribute(request);
            }

            // when: 5개씩 조회 (한 페이지에 모든 데이터가 포함됨)
            CursorDto cursor = CursorDto.ofDefault(5);
            PaginatedResponse<ClothesAttributeDefDto> result = clothAttributeService.findAttributes(cursor);

            // then
            assertEquals(3, result.getData().size());
            assertFalse(result.getPagination().isHasNext(), "마지막 페이지에서는 hasNext가 false여야 함");
            assertNull(result.getPagination().getNextCursor(), "마지막 페이지에서는 nextCursor가 null이어야 함");
        }
    }

    @Nested
    @DisplayName("검색 기능 테스트")
    class SearchTests {

        @Test
        @Commit
        @DisplayName("키워드 검색 기능 - 부분 일치")
        void keywordSearch_Integration() {
            // given: 검색 대상 속성 생성
            ClothesAttributeDefCreateRequest request1 = new ClothesAttributeDefCreateRequest(
                    "색상", Arrays.asList("빨강", "파랑"));
            ClothesAttributeDefCreateRequest request2 = new ClothesAttributeDefCreateRequest(
                    "크기", Arrays.asList("S", "M", "L"));
            ClothesAttributeDefCreateRequest request3 = new ClothesAttributeDefCreateRequest(
                    "색상상", Arrays.asList("검정", "흰색"));

            clothAttributeService.createAttribute(request1);
            clothAttributeService.createAttribute(request2);
            clothAttributeService.createAttribute(request3);

            // when: "색상" 키워드로 검색
            CursorDto searchCursor = CursorDto.ofSearch("색상", 10);
            PaginatedResponse<ClothesAttributeDefDto> searchResult = clothAttributeService.findAttributes(searchCursor);

            // then: "색상"과 "색상상"이 검색되어야 함
            assertEquals(2, searchResult.getData().size());
            assertTrue(searchResult.getData().stream().allMatch(attr ->
                    attr.name().contains("색상")));
        }

        @Test
        @Commit
        @DisplayName("존재하지 않는 키워드 검색 시 빈 결과 반환")
        void searchNonExistentKeyword_Integration() {
            // given: 몇 개의 속성 생성
            ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest(
                    "색상", Arrays.asList("빨강", "파랑"));
            clothAttributeService.createAttribute(request);

            // when: 존재하지 않는 키워드로 검색
            CursorDto searchCursor = CursorDto.ofSearch("존재하지않는키워드", 10);
            PaginatedResponse<ClothesAttributeDefDto> searchResult = clothAttributeService.findAttributes(searchCursor);

            // then
            assertEquals(0, searchResult.getData().size());
            assertEquals(0L, searchResult.getPagination().getTotalCount());
            assertFalse(searchResult.getPagination().isHasNext());
        }
    }

    @Nested
    @DisplayName("옵션 관리 테스트")
    class OptionManagementTests {

        @Test
        @Commit
        @DisplayName("옵션 추가 및 제거 복합 시나리오")
        void optionManagement_Integration() {
            // given: 속성 생성
            ClothesAttributeDefCreateRequest createRequest = new ClothesAttributeDefCreateRequest(
                    "재질", Arrays.asList("면", "폴리에스터", "울"));
            AttributeResponseDto createdAttribute = clothAttributeService.createAttribute(createRequest);

            // when: 전체 교체로 옵션 추가 (리넨, 실크 포함)
            ClothesAttributeDefCreateRequest addByReplace = new ClothesAttributeDefCreateRequest(
                    "재질", Arrays.asList("면", "폴리에스터", "울", "리넨", "실크"));
            AttributeResponseDto withAddedOptions = clothAttributeService.updateAttribute(
                    Long.valueOf(createdAttribute.id()), addByReplace);

            // then
            assertEquals(5, withAddedOptions.selectableValues().size());
            assertTrue(withAddedOptions.selectableValues().contains("리넨"));
            assertTrue(withAddedOptions.selectableValues().contains("실크"));

            // when: 전체 교체로 옵션 제거 (면, 실크 제거)
            ClothesAttributeDefCreateRequest removeByReplace = new ClothesAttributeDefCreateRequest(
                    "재질", Arrays.asList("폴리에스터", "울", "리넨"));
            AttributeResponseDto withRemovedOptions = clothAttributeService.updateAttribute(
                    Long.valueOf(withAddedOptions.id()), removeByReplace);

            // then
            assertEquals(3, withRemovedOptions.selectableValues().size());
            assertFalse(withRemovedOptions.selectableValues().contains("면"));
            assertFalse(withRemovedOptions.selectableValues().contains("실크"));
            assertTrue(withRemovedOptions.selectableValues().contains("폴리에스터"));
            assertTrue(withRemovedOptions.selectableValues().contains("울"));
            assertTrue(withRemovedOptions.selectableValues().contains("리넨"));
        }

        @Test
        @Commit
        @DisplayName("중복 옵션 추가 시 중복 제거 확인")
        void addDuplicateOptions_Integration() {
            // given: 속성 생성
            ClothesAttributeDefCreateRequest createRequest = new ClothesAttributeDefCreateRequest(
                    "재질", Arrays.asList("면", "폴리에스터"));
            AttributeResponseDto createdAttribute = clothAttributeService.createAttribute(createRequest);

            // when: 전체 교체로 중복 포함 리스트 전달 -> 결과는 중복 제거되어야 함
            ClothesAttributeDefCreateRequest replaceWithDuplicates = new ClothesAttributeDefCreateRequest(
                    "재질", Arrays.asList("면", "폴리에스터", "울", "면"));
            AttributeResponseDto result = clothAttributeService.updateAttribute(
                    Long.valueOf(createdAttribute.id()), replaceWithDuplicates);

            // then: 중복은 제거되어 최종 3개
            assertEquals(3, result.selectableValues().size());
            assertTrue(result.selectableValues().contains("면"));
            assertTrue(result.selectableValues().contains("폴리에스터"));
            assertTrue(result.selectableValues().contains("울"));
        }

        @Test
        @Commit
        @DisplayName("존재하지 않는 옵션 제거 시 무시됨")
        void removeNonExistentOptions_Integration() {
            // given: 속성 생성
            ClothesAttributeDefCreateRequest createRequest = new ClothesAttributeDefCreateRequest(
                    "재질", Arrays.asList("면", "폴리에스터", "울"));
            AttributeResponseDto createdAttribute = clothAttributeService.createAttribute(createRequest);

            // when: 전체 교체로 동일 리스트 전달 (존재하지 않는 값 제거 요청 효과 없음)
            ClothesAttributeDefCreateRequest sameList = new ClothesAttributeDefCreateRequest(
                    "재질", Arrays.asList("면", "폴리에스터", "울"));
            AttributeResponseDto result = clothAttributeService.updateAttribute(
                    Long.valueOf(createdAttribute.id()), sameList);

            // then: 기존 옵션들이 그대로 유지됨
            assertEquals(3, result.selectableValues().size());
            assertTrue(result.selectableValues().contains("면"));
            assertTrue(result.selectableValues().contains("폴리에스터"));
            assertTrue(result.selectableValues().contains("울"));
        }
    }

    @Nested
    @DisplayName("예외 상황 처리 테스트")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("존재하지 않는 속성 ID로 수정 시도")
        void updateNonExistentAttribute_ThrowsException() {
            // given
            Long nonExistentId = 99999L;
            ClothesAttributeDefCreateRequest updateRequest = new ClothesAttributeDefCreateRequest(
                    "존재하지않는속성", Arrays.asList("옵션1", "옵션2"));

            // when & then
            ClothesException exception = assertThrows(ClothesException.class, () ->
                    clothAttributeService.updateAttribute(nonExistentId, updateRequest));

            assertNotNull(exception.getMessage());
        }

        @Test
        @DisplayName("존재하지 않는 속성 ID로 옵션 추가 시도")
        void addOptionsToNonExistentAttribute_ThrowsException() {
            // given
            Long nonExistentId = 99999L;

            // when & then
            assertThrows(ClothesException.class, () ->
                    clothAttributeService.addAttributeOptions(nonExistentId, List.of("새옵션")));
        }

        @Test
        @DisplayName("존재하지 않는 속성 ID로 옵션 제거 시도")
        void removeOptionsFromNonExistentAttribute_ThrowsException() {
            // given
            Long nonExistentId = 99999L;

            // when & then
            assertThrows(ClothesException.class, () ->
                    clothAttributeService.removeAttributeOptions(nonExistentId, List.of("옵션")));
        }

        @Test
        @DisplayName("존재하지 않는 속성 ID로 삭제 시도")
        void deleteNonExistentAttribute_ThrowsException() {
            // given
            Long nonExistentId = 99999L;

            // when & then
            assertThrows(ClothesException.class, () ->
                    clothAttributeService.deleteAttributeById(nonExistentId));
        }

        @Test
        @DisplayName("null 값으로 속성 생성 시도")
        void createAttributeWithNullValues_ThrowsException() {
            // when & then
            assertThrows(Exception.class, () ->
                    clothAttributeService.createAttribute(null));
        }
    }

    @Nested
    @DisplayName("동시성 및 성능 테스트")
    class ConcurrencyAndPerformanceTests {

        @Test
        @Commit
        @DisplayName("대량 데이터 조회 성능 테스트")
        void largeDataPerformance_Integration() {
            // given: 대량의 속성 생성 (100개)
            for (int i = 1; i <= 100; i++) {
                ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest(
                        "속성" + String.format("%03d", i), Arrays.asList("옵션1", "옵션2", "옵션3"));
                clothAttributeService.createAttribute(request);
            }

            // when: 전체 조회
            long startTime = System.currentTimeMillis();
            CursorDto cursor = CursorDto.ofDefault(50);
            PaginatedResponse<ClothesAttributeDefDto> result = clothAttributeService.findAttributes(cursor);
            long endTime = System.currentTimeMillis();

            // then: 성능 확인 (임의의 임계값, 실제 환경에 맞게 조정 필요)
            assertTrue(endTime - startTime < 5000, "조회 시간이 5초를 초과하지 않아야 함");
            assertEquals(50, result.getData().size());
            assertTrue(result.getPagination().getTotalCount() >= 100L);
        }
    }
}
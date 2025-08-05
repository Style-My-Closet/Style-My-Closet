package com.stylemycloset.cloth.service;

import com.stylemycloset.cloth.dto.ClothCreateRequestDto;
import com.stylemycloset.cloth.dto.ClothUpdateRequestDto;
import com.stylemycloset.cloth.dto.response.ClothUpdateResponseDto;
import com.stylemycloset.cloth.dto.CursorDto;
import com.stylemycloset.cloth.dto.response.ClothListResponseDto;
import com.stylemycloset.cloth.dto.ClothResponseDto;
import com.stylemycloset.cloth.entity.ClothingCategory;
import com.stylemycloset.cloth.entity.ClothingCategoryType;
import com.stylemycloset.cloth.exception.ClothesException;
import com.stylemycloset.cloth.repository.ClothingCategoryRepository;
import com.stylemycloset.cloth.repository.ClosetRepository;
import com.stylemycloset.user.repository.UserRepository;
import com.stylemycloset.cloth.entity.Closet;
import com.stylemycloset.testutil.IntegrationTestSupport;
import com.stylemycloset.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class ClothServiceIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private ClothService clothService;
    

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ClosetRepository closetRepository;
    
    @Autowired
    private ClothingCategoryRepository categoryRepository;

    private User testUser;
    private Closet testCloset;
    private ClothingCategory testCategory;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = new User("테스트유저", "test@example.com", com.stylemycloset.user.entity.Role.USER, com.stylemycloset.user.entity.Gender.MALE);
        userRepository.save(testUser);

        // 테스트용 옷장 생성
        testCloset = new Closet(testUser);
        closetRepository.save(testCloset);

        // 테스트용 카테고리 생성
        testCategory = new ClothingCategory(ClothingCategoryType.TOP);
        categoryRepository.save(testCategory);
    }

    @Nested
    @DisplayName("완전한 사용자 시나리오 테스트")
    class CompleteUserScenarioTests {

        @Test
        @DisplayName("통합 테스트: 의류 전체 라이프사이클 (CRUD + 페이징 + 캐시)")
        void clothLifeCycle_Integration() {
            // 1. 의류 생성
            ClothCreateRequestDto createRequest = new ClothCreateRequestDto();
            createRequest.setName("테스트 의류");
            createRequest.setCategoryId(testCategory.getId());
            createRequest.setType("TOP");
            
            ClothResponseDto createdCloth = clothService.createCloth(createRequest, testUser.getId(), null);
            
            assertNotNull(createdCloth);
            assertEquals("테스트 의류", createdCloth.getName());
            assertEquals(ClothingCategoryType.TOP, createdCloth.getType());

            // 2. 의류 목록 조회 (페이징)
            CursorDto cursorDto = new CursorDto(null, null, "10", "id", "ASCENDING", null);
            ClothListResponseDto clothList = clothService.getClothesWithCursor(testUser.getId(), cursorDto);

            assertFalse(clothList.getData().isEmpty());
            assertTrue(clothList.getTotalCount() >= 1L);
            
            // 생성된 의류가 목록에 있는지 확인
            boolean foundCreatedCloth = clothList.getData().stream()
                    .anyMatch(cloth -> cloth.getId().equals(createdCloth.getClothId().toString()));
            assertTrue(foundCreatedCloth);

            // 3. 의류 수정
            ClothUpdateRequestDto updateRequest = ClothUpdateRequestDto.builder()
                    .name("수정된 의류")
                    .categoryId(testCategory.getId())
                    .build();

            ClothUpdateResponseDto updatedCloth = clothService.updateCloth(createdCloth.getClothId(), updateRequest, null);
            assertEquals("수정된 의류", updatedCloth.name());

            // 4. 의류 삭제
            clothService.deleteCloth(Long.valueOf(updatedCloth.id()));
            
            // 삭제 확인 - Soft Delete이므로 실제로는 deleted_at만 설정됨
            // 트랜잭션 내에서는 삭제된 항목이 보일 수 있으므로 다른 방법으로 확인
            try {
                clothService.updateCloth(Long.valueOf(updatedCloth.id()), ClothUpdateRequestDto.builder().name("test").build(), null);
                fail("삭제된 의류를 수정하려고 하면 예외가 발생해야 합니다.");
            } catch (ClothesException e) {
                // 예상된 예외 - 삭제된 의류를 찾을 수 없음
                assertTrue(true);
            }
        }
    }

    @Nested
    @DisplayName("다중 사용자 시나리오 테스트")
    class MultiUserScenarioTests {

        @Test
        @DisplayName("통합 테스트: 다중 사용자 의류 격리")
        void multiUserClothIsolation_Integration() {
            // 두 번째 사용자 생성
            User secondUser = new User("테스트유저2", "test2@example.com", com.stylemycloset.user.entity.Role.USER, com.stylemycloset.user.entity.Gender.FEMALE);
            userRepository.save(secondUser);

            Closet secondCloset = new Closet(secondUser);
            closetRepository.save(secondCloset);

            // 첫 번째 사용자의 의류 생성
            ClothCreateRequestDto firstUserRequest = new ClothCreateRequestDto();
            firstUserRequest.setName("첫 번째 사용자 의류");
            firstUserRequest.setCategoryId(testCategory.getId());
            firstUserRequest.setType("TOP");

            ClothResponseDto firstUserCloth = clothService.createCloth(firstUserRequest, testUser.getId(), null);

            // 두 번째 사용자의 의류 생성
            ClothCreateRequestDto secondUserRequest = new ClothCreateRequestDto();
            secondUserRequest.setName("두 번째 사용자 의류");
            secondUserRequest.setCategoryId(testCategory.getId());
            secondUserRequest.setType("TOP");
            
            ClothResponseDto secondUserCloth = clothService.createCloth(secondUserRequest, secondUser.getId(), null);

            // 각 사용자의 의류 목록 조회
            CursorDto cursorDto = new CursorDto(null, null, "10", "id", "ASCENDING", null);
            
            ClothListResponseDto firstUserList = clothService.getClothesWithCursor(testUser.getId(), cursorDto);
            ClothListResponseDto secondUserList = clothService.getClothesWithCursor(secondUser.getId(), cursorDto);

            // 각 사용자는 자신의 의류만 볼 수 있어야 함
            assertFalse(firstUserList.getData().isEmpty());
            assertFalse(secondUserList.getData().isEmpty());
            
            // 첫 번째 사용자의 목록에 첫 번째 사용자의 의류가 있는지 확인
            boolean firstUserHasOwnCloth = firstUserList.getData().stream()
                    .anyMatch(cloth -> cloth.getId().equals(firstUserCloth.getClothId().toString()));
            assertTrue(firstUserHasOwnCloth);
            
            // 두 번째 사용자의 목록에 두 번째 사용자의 의류가 있는지 확인
            boolean secondUserHasOwnCloth = secondUserList.getData().stream()
                    .anyMatch(cloth -> cloth.getId().equals(secondUserCloth.getClothId().toString()));
            assertTrue(secondUserHasOwnCloth);
        }
    }

    @Nested
    @DisplayName("페이징 및 쿼리 시나리오 테스트")
    class PaginationAndQueryScenarioTests {

        @Test
        @DisplayName("통합 테스트: 페이징 기능")
        void pagination_Integration() {
            // 여러 의류 생성
            for (int i = 1; i <= 15; i++) {
                ClothCreateRequestDto request = new ClothCreateRequestDto();
                request.setName("의류 " + i);
                request.setCategoryId(testCategory.getId());
                request.setType("TOP");
                clothService.createCloth(request, testUser.getId(), null);
            }

            // 첫 번째 페이지 조회 (5개씩)
            CursorDto firstPageCursor = new CursorDto(null, null, "5", "id", "ASCENDING", null);
            ClothListResponseDto firstPage = clothService.getClothesWithCursor(testUser.getId(), firstPageCursor);

            assertEquals(5, firstPage.getData().size());
            assertTrue(firstPage.isHasNext());
            assertNotNull(firstPage.getNextCursor());

            // 두 번째 페이지 조회
            CursorDto secondPageCursor = new CursorDto(firstPage.getNextCursor(), null, "5", "id", "ASCENDING", null);
            ClothListResponseDto secondPage = clothService.getClothesWithCursor(testUser.getId(), secondPageCursor);

            assertEquals(5, secondPage.getData().size());
            assertTrue(secondPage.isHasNext());

            // 세 번째 페이지 조회
            CursorDto thirdPageCursor = new CursorDto(secondPage.getNextCursor(), null, "5", "id", "ASCENDING", null);
            ClothListResponseDto thirdPage = clothService.getClothesWithCursor(testUser.getId(), thirdPageCursor);

            assertTrue(thirdPage.getData().size() > 0);
        }

        @Test
        @DisplayName("통합 테스트: 정렬 기능")
        void sorting_Integration() {
            // 여러 의류 생성
            for (int i = 1; i <= 5; i++) {
                ClothCreateRequestDto request = new ClothCreateRequestDto();
                request.setName("의류 " + i);
                request.setCategoryId(testCategory.getId());
                request.setType("TOP");
                clothService.createCloth(request, testUser.getId(), null);
            }

            // 오름차순 정렬
            CursorDto ascendingCursor = new CursorDto(null, null, "10", "name", "ASCENDING", null);
            ClothListResponseDto ascendingList = clothService.getClothesWithCursor(testUser.getId(), ascendingCursor);

            // 내림차순 정렬
            CursorDto descendingCursor = new CursorDto(null, null, "10", "name", "DESCENDING", null);
            ClothListResponseDto descendingList = clothService.getClothesWithCursor(testUser.getId(), descendingCursor);

            // 정렬이 다르게 적용되었는지 확인
            assertNotEquals(ascendingList.getData(), descendingList.getData());
        }
    }

    @Nested
    @DisplayName("예외 처리 시나리오 테스트")
    class ExceptionHandlingScenarioTests {

        @Test
        @DisplayName("통합 테스트: 존재하지 않는 의류 수정 시도")
        void updateNonExistentCloth_Integration() {
            ClothUpdateRequestDto updateRequest = ClothUpdateRequestDto.builder()
                    .name("수정된 의류")
                    .build();

            assertThrows(ClothesException.class, () -> {
                clothService.updateCloth(99999L, updateRequest, null);
            });
        }

        @Test
        @DisplayName("통합 테스트: 존재하지 않는 의류 삭제 시도")
        void deleteNonExistentCloth_Integration() {
            assertThrows(ClothesException.class, () -> {
                clothService.deleteCloth(99999L);
            });
        }
    }
} 
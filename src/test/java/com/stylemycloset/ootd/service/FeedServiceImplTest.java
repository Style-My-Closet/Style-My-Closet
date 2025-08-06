package com.stylemycloset.ootd.service;

import com.stylemycloset.cloth.entity.Cloth;
import com.stylemycloset.cloth.entity.ClothingCategory;
import com.stylemycloset.cloth.entity.ClothingCategoryType;
import com.stylemycloset.cloth.repository.ClothRepository;
import com.stylemycloset.common.dto.CursorResponse;
import com.stylemycloset.ootd.dto.FeedCreateRequest;
import com.stylemycloset.ootd.dto.FeedDto;
import com.stylemycloset.ootd.entity.Feed;
import com.stylemycloset.ootd.repo.FeedClothesRepository;
import com.stylemycloset.ootd.repo.FeedRepository;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import com.stylemycloset.weather.repository.WeatherRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedServiceImplTest {

  @InjectMocks
  private FeedServiceImpl feedService;

  @Mock
  private UserRepository userRepository;
  @Mock
  private ClothRepository clothRepository;
  @Mock
  private FeedRepository feedRepository;
  @Mock
  private FeedClothesRepository feedClothesRepository;
  @Mock
  private WeatherRepository weatherRepository;

  @Nested
  @DisplayName("피드 생성")
  class CreateFeed {

    @Test
    @DisplayName("성공")
    void success() {
      // given (준비)
      Long authorId = 1L;
      List<Long> clothesIds = List.of(101L, 102L);
      FeedCreateRequest request = new FeedCreateRequest(authorId, null, clothesIds, "테스트 피드 내용");

      // Mock 객체 및 반환값 설정
      User mockUser = mock(User.class);
      Cloth mockCloth1 = mock(Cloth.class);
      Cloth mockCloth2 = mock(Cloth.class);
      ClothingCategory mockCategory = mock(ClothingCategory.class);

      when(userRepository.findById(authorId)).thenReturn(Optional.of(mockUser));
      when(clothRepository.findAllById(clothesIds)).thenReturn(List.of(mockCloth1, mockCloth2));
      when(feedRepository.save(any(Feed.class))).thenAnswer(invocation -> {
        Feed feed = invocation.getArgument(0);
        ReflectionTestUtils.setField(feed, "id", 1L);
        ReflectionTestUtils.setField(feed, "createdAt", Instant.now());
        ReflectionTestUtils.setField(feed, "updatedAt", Instant.now());
        return feed;
      });

      // Mock 객체의 상세 행동 정의
      when(mockUser.getId()).thenReturn(authorId);
      when(mockUser.getName()).thenReturn("테스트유저");
      when(mockCloth1.getId()).thenReturn(101L);
      when(mockCloth1.getName()).thenReturn("청바지");
      when(mockCloth1.getCategory()).thenReturn(mockCategory);
      when(mockCloth2.getId()).thenReturn(102L);
      when(mockCloth2.getName()).thenReturn("흰티셔츠");
      when(mockCloth2.getCategory()).thenReturn(mockCategory);
      when(mockCategory.getName()).thenReturn(ClothingCategoryType.TOP);

      // when (실행)
      FeedDto result = feedService.createFeed(request);

      // then (검증)
      assertThat(result).isNotNull();
      assertThat(result.content()).isEqualTo("테스트 피드 내용");
      assertThat(result.author().userId()).isEqualTo(authorId);
      assertThat(result.ootds()).hasSize(2);
      assertThat(result.ootds().get(0).name()).isEqualTo("청바지");

      // verify (행위 검증)
      verify(feedRepository, times(1)).save(any(Feed.class));
      verify(feedClothesRepository, times(1)).saveAll(any());
    }
  }

  // ✅ =============================================================
  // ✅ 여기에 '피드 목록 조회' 단위 테스트가 새로 추가되었어요!
  // ✅ =============================================================
  @Nested
  @DisplayName("피드 목록 조회")
  class GetFeeds {

    @Test
    @DisplayName("성공")
    void success() {
      // given (준비)
      Long cursorId = null;
      Pageable pageable = PageRequest.of(0, 10);

      // Repository가 반환할 '가짜' 엔티티 목록 생성
      Feed mockFeed = mock(Feed.class);
      User mockUser = mock(User.class);
      when(mockFeed.getAuthor()).thenReturn(mockUser); // ✅ Feed가 User를 반환하도록 설정
      List<Feed> fakeFeeds = List.of(mockFeed);

      // 🧠 "findByConditions 메서드가 호출되면, 우리가 만든 가짜 목록을 돌려줘!" 라고 설정
      when(feedRepository.findByConditions(cursorId, null, null, null, pageable))
          .thenReturn(fakeFeeds);

      // when (실행)
      CursorResponse<FeedDto> result = feedService.getFeeds(cursorId, null, null, null, pageable);

      // then (검증)
      assertThat(result).isNotNull();
      assertThat(result.hasNext()).isFalse(); // 1개만 조회했으므로 다음 페이지 없음
      assertThat(result.data()).hasSize(1);
    }
  }
}

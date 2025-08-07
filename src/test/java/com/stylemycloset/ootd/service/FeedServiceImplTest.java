package com.stylemycloset.ootd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stylemycloset.cloth.entity.Cloth;
import com.stylemycloset.cloth.entity.ClothingCategory;
import com.stylemycloset.cloth.entity.ClothingCategoryType;
import com.stylemycloset.cloth.repository.ClothRepository;
import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import com.stylemycloset.ootd.dto.FeedCreateRequest;
import com.stylemycloset.ootd.dto.FeedDto;
import com.stylemycloset.ootd.dto.FeedDtoCursorResponse;
import com.stylemycloset.ootd.entity.Feed;
import com.stylemycloset.ootd.entity.FeedLike;
import com.stylemycloset.ootd.repo.FeedClothesRepository;
import com.stylemycloset.ootd.repo.FeedLikeRepository;
import com.stylemycloset.ootd.repo.FeedRepository;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import com.stylemycloset.weather.repository.WeatherRepository;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
  @Mock
  private FeedLikeRepository feedLikeRepository;

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
      // verify(feedClothesRepository, times(1)).saveAll(any());
    }
  }

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
      when(mockFeed.getAuthor()).thenReturn(mockUser);
      List<Feed> fakeFeeds = List.of(mockFeed);

      when(feedRepository.findByConditions(cursorId, null, null, null, pageable))
          .thenReturn(fakeFeeds);

      // when (실행)
      FeedDtoCursorResponse result = feedService.getFeeds(cursorId, null, null, null, pageable);

      // then (검증)
      assertThat(result).isNotNull();
      assertThat(result.hasNext()).isFalse(); // 1개만 조회했으므로 다음 페이지 없음
      assertThat(result.data()).hasSize(1);
    }
  }

  @Nested
  @DisplayName("피드 좋아요")
  class LikeFeed {

    @Test
    @DisplayName("성공 - 피드 좋아요 누르기")
    void likeFeedSuccess() {
      Long userId = 1L;
      Long feedId = 10L;
      User mockUser = mock(User.class);
      Feed mockFeed = mock(Feed.class);
      User author = mock(User.class); // 피드 작성자 Mock

      when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
      when(feedRepository.findById(feedId)).thenReturn(Optional.of(mockFeed));

      when(feedLikeRepository.findByUserAndFeed(mockUser, mockFeed)).thenReturn(Optional.empty());

      when(mockFeed.getAuthor()).thenReturn(author);
      when(author.getId()).thenReturn(2L);
      when(mockFeed.getFeedClothes()).thenReturn(Collections.emptyList());
      when(feedLikeRepository.countByFeed(mockFeed)).thenReturn(1L); // 좋아요 후 카운트는 1
      when(feedLikeRepository.existsByUserAndFeed(mockUser, mockFeed)).thenReturn(
          true); // 좋아요 후에는 존재함

      FeedDto result = feedService.likeFeed(userId, feedId);

      verify(feedLikeRepository, times(1)).save(any(FeedLike.class)); // save가 1번 호출되었는지 검증
      assertThat(result.likeCount()).isEqualTo(1L);
      assertThat(result.likedByMe()).isTrue();
    }

    @Test
    @DisplayName("성공 - 피드 좋아요 취소하기")
    void unlikeFeedSuccess() {
      // given (준비)
      Long userId = 1L;
      Long feedId = 10L;
      User mockUser = mock(User.class);
      Feed mockFeed = mock(Feed.class);
      FeedLike mockFeedLike = mock(FeedLike.class);

      // Mock 객체 설정
      when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
      when(feedRepository.findById(feedId)).thenReturn(Optional.of(mockFeed));
      // 이미 좋아요를 누른 상태를 가정
      when(feedLikeRepository.findByUserAndFeed(mockUser, mockFeed)).thenReturn(
          Optional.of(mockFeedLike));

      // when (실행)
      feedService.unlikeFeed(userId, feedId);

      // then (검증)
      verify(feedLikeRepository, times(1)).delete(mockFeedLike); // delete가 1번 호출되었는지 검증
    }

    @Test
    @DisplayName("실패 - 이미 좋아요를 누른 피드에 다시 좋아요 요청")
    void likeFeedFail_AlreadyLiked() {
      // given (준비)
      Long userId = 1L;
      Long feedId = 10L;
      User mockUser = mock(User.class);
      Feed mockFeed = mock(Feed.class);
      FeedLike mockFeedLike = mock(FeedLike.class);

      // Mock 객체 설정
      when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
      when(feedRepository.findById(feedId)).thenReturn(Optional.of(mockFeed));
      // 이미 좋아요를 누른 상태를 가정
      when(feedLikeRepository.findByUserAndFeed(mockUser, mockFeed)).thenReturn(
          Optional.of(mockFeedLike));

      // when & then (실행 및 검증)
      // StyleMyClosetException이 발생하는지, ErrorCode가 ALREADY_LIKED_FEED인지 확인
      assertThatThrownBy(() -> feedService.likeFeed(userId, feedId))
          .isInstanceOf(StyleMyClosetException.class)
          .extracting("errorCode")
          .isEqualTo(ErrorCode.ALREADY_LIKED_FEED);
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 유저의 요청")
    void fail_UserNotFound() {
      // given (준비)
      Long userId = 999L; // 존재하지 않는 유저 ID
      Long feedId = 10L;
      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      // when & then (좋아요 요청)
      assertThatThrownBy(() -> feedService.likeFeed(userId, feedId))
          .isInstanceOf(StyleMyClosetException.class)
          .extracting("errorCode")
          .isEqualTo(ErrorCode.USER_NOT_FOUND);

      // when & then (좋아요 취소 요청)
      assertThatThrownBy(() -> feedService.unlikeFeed(userId, feedId))
          .isInstanceOf(StyleMyClosetException.class)
          .extracting("errorCode")
          .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 피드")
    void fail_FeedNotFound() {
      // given (준비)
      Long userId = 1L;
      Long feedId = 999L; // 존재하지 않는 피드 ID
      User mockUser = mock(User.class);
      when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
      when(feedRepository.findById(feedId)).thenReturn(Optional.empty());

      // when & then (좋아요 요청)
      assertThatThrownBy(() -> feedService.likeFeed(userId, feedId))
          .isInstanceOf(StyleMyClosetException.class)
          .extracting("errorCode")
          .isEqualTo(ErrorCode.FEED_NOT_FOUND);

      // when & then (좋아요 취소 요청)
      assertThatThrownBy(() -> feedService.unlikeFeed(userId, feedId))
          .isInstanceOf(StyleMyClosetException.class)
          .extracting("errorCode")
          .isEqualTo(ErrorCode.FEED_NOT_FOUND);
    }
  }
}
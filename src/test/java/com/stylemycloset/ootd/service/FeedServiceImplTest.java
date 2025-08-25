package com.stylemycloset.ootd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
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
import com.stylemycloset.notification.event.domain.FeedLikedEvent;
import com.stylemycloset.ootd.dto.FeedCreateRequest;
import com.stylemycloset.ootd.dto.FeedDto;
import com.stylemycloset.ootd.dto.FeedDtoCursorResponse;
import com.stylemycloset.ootd.dto.FeedSearchRequest;
import com.stylemycloset.ootd.dto.FeedUpdateRequest;
import com.stylemycloset.ootd.entity.Feed;
import com.stylemycloset.ootd.entity.FeedLike;
import com.stylemycloset.ootd.repo.FeedClothesRepository;
import com.stylemycloset.ootd.repo.FeedLikeRepository;
import com.stylemycloset.ootd.repo.FeedRepository;
import com.stylemycloset.ootd.mapper.OotdItemMapper;
import com.stylemycloset.ootd.mapper.FeedMapper;
import com.stylemycloset.ootd.mapper.CommentMapper;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import com.stylemycloset.weather.repository.WeatherRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
  @Mock
  private ApplicationEventPublisher publisher;
  @Mock
  private OotdItemMapper ootdItemMapper;
  @Mock
  private FeedMapper feedMapper;
  @Mock
  private CommentMapper commentMapper;

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

      // Mock 매퍼 응답 설정
      FeedDto mockFeedDto = mock(FeedDto.class);
      when(feedMapper.toDto(any(Feed.class), any(User.class), anyLong(), anyInt(), anyBoolean()))
          .thenReturn(mockFeedDto);

      // when (실행)
      FeedDto result = feedService.createFeed(request);

      // then (검증)
      assertThat(result).isNotNull();
      verify(feedMapper, times(1)).toDto(any(Feed.class), any(User.class), anyLong(), anyInt(), anyBoolean());

      // verify (행위 검증)
      // Feed 엔티티 내부에서 cascade로 저장되는지 확인
      ArgumentCaptor<Feed> feedCaptor = ArgumentCaptor.forClass(Feed.class);
      verify(feedRepository, times(1)).save(feedCaptor.capture());
      assertThat(feedCaptor.getValue().getFeedClothes()).hasSize(2);
    }
  }

  @Nested
  @DisplayName("피드 목록 조회")
  class GetFeeds {

    @Test
    @DisplayName("성공")
    void success() {
      // given (준비)
      FeedSearchRequest request = FeedSearchRequest.builder().limit(10).build();

      // Repository가 반환할 '가짜' 엔티티 목록 생성
      Feed mockFeed = mock(Feed.class);
      User mockUser = mock(User.class);
      when(mockFeed.getAuthor()).thenReturn(mockUser);
      List<Feed> fakeFeeds = List.of(mockFeed);

      when(feedRepository.findByConditions(request))
          .thenReturn(fakeFeeds);

      // Mock 좋아요 관련 repository 호출
      when(feedLikeRepository.countByFeedIds(anyList())).thenReturn(Collections.emptyList());
      when(feedLikeRepository.findFeedIdsByUserAndFeedIds(anyLong(), anyList())).thenReturn(Collections.emptyList());

      // Mock 매퍼 응답 설정
      List<FeedDto> mockFeedDtos = List.of(mock(FeedDto.class));
      when(feedMapper.toDtoList(anyList(), any(), anyMap(), anyMap(), anyMap()))
          .thenReturn(mockFeedDtos);

      // when (실행)
      FeedDtoCursorResponse result = feedService.getFeeds(request, null);

      // then (검증)
      assertThat(result).isNotNull();
      verify(feedMapper, times(1)).toDtoList(anyList(), any(), anyMap(), anyMap(), anyMap());
    }
  }

  @Test
  @DisplayName("성공 - 다음 페이지가 존재하는 경우")
  void success_HasNext() {
    // given
    int limit = 10;
    FeedSearchRequest request = FeedSearchRequest.builder()
        .limit(limit)
        .sortBy("createdAt")
        .build();

    List<Feed> fakeFeeds = new ArrayList<>();
    for (int i = 0; i < limit + 1; i++) {
      Feed mockFeed = mock(Feed.class);
      User mockUser = mock(User.class);
      when(mockFeed.getAuthor()).thenReturn(mockUser);
      when(mockFeed.getFeedClothes()).thenReturn(Collections.emptyList());
      when(mockFeed.getId()).thenReturn((long) (limit - i));
      when(mockFeed.getCreatedAt()).thenReturn(Instant.now().minusSeconds(i));
      fakeFeeds.add(mockFeed);
    }

    when(feedRepository.findByConditions(request)).thenReturn(fakeFeeds);

    // Mock 좋아요 관련 repository 호출
    when(feedLikeRepository.countByFeedIds(anyList())).thenReturn(Collections.emptyList());
    when(feedLikeRepository.findFeedIdsByUserAndFeedIds(anyLong(), anyList())).thenReturn(Collections.emptyList());

    // Mock 매퍼 응답 설정
    List<FeedDto> mockFeedDtos = new ArrayList<>();
    for (int i = 0; i < limit; i++) {
      mockFeedDtos.add(mock(FeedDto.class));
    }
    when(feedMapper.toDtoList(anyList(), any(), anyMap(), anyMap(), anyMap()))
        .thenReturn(mockFeedDtos);

    // when
    FeedDtoCursorResponse result = feedService.getFeeds(request, null);

    // then
    verify(feedMapper, times(1)).toDtoList(anyList(), any(), anyMap(), anyMap(), anyMap());
  }

  @Test
  @DisplayName("좋아요 토글 - 성공 (좋아요 추가)")
  void toggleLike_whenNotLiked_createsLike() {
    // given
    Long userId = 1L;
    Long feedId = 10L;
    User mockUser = mock(User.class);
    Feed mockFeed = mock(Feed.class);
    when(userRepository.findByIdAndDeletedAtIsNullAndLockedIsFalse(userId)).thenReturn(
        Optional.of(mockUser));
    when(feedRepository.findById(feedId)).thenReturn(Optional.of(mockFeed));
    when(feedLikeRepository.findByUserAndFeed(mockUser, mockFeed)).thenReturn(Optional.empty());
    when(mockFeed.getAuthor()).thenReturn(mock(User.class));
    when(mockFeed.getId()).thenReturn(feedId);
    when(mockUser.getId()).thenReturn(userId);
    
    // Mock 매퍼 응답 설정 (toggleLike에서는 매퍼가 호출되지 않지만, 안전을 위해 설정)
    FeedDto mockFeedDto = mock(FeedDto.class);
    when(feedMapper.toDto(any(Feed.class), any(User.class), anyLong(), anyInt(), anyBoolean()))
        .thenReturn(mockFeedDto);
    
    // when
    feedService.toggleLike(userId, feedId);

    // then
    verify(feedLikeRepository, times(1)).save(any(FeedLike.class));
    verify(feedLikeRepository, times(0)).delete(any());
    verify(publisher).publishEvent(any(FeedLikedEvent.class));
  }

  @Test
  @DisplayName("좋아요 토글 - 성공 (좋아요 취소)")
  void toggleLike_whenAlreadyLiked_deletesLike() {
    // given
    Long userId = 1L;
    Long feedId = 10L;
    User mockUser = mock(User.class);
    Feed mockFeed = mock(Feed.class);
    FeedLike mockFeedLike = mock(FeedLike.class);
    when(userRepository.findByIdAndDeletedAtIsNullAndLockedIsFalse(userId)).thenReturn(
        Optional.of(mockUser));
    when(feedRepository.findById(feedId)).thenReturn(Optional.of(mockFeed));
    when(feedLikeRepository.findByUserAndFeed(mockUser, mockFeed)).thenReturn(
        Optional.of(mockFeedLike));
    when(mockFeed.getAuthor()).thenReturn(mock(User.class));

        // Mock 매퍼 응답 설정 (toggleLike에서는 매퍼가 호출되지 않지만, 안전을 위해 설정)
    FeedDto mockFeedDto = mock(FeedDto.class);
    when(feedMapper.toDto(any(Feed.class), any(User.class), anyLong(), anyInt(), anyBoolean()))
        .thenReturn(mockFeedDto);
    
    // when
    feedService.toggleLike(userId, feedId);

    // then
    verify(feedLikeRepository, times(1)).delete(mockFeedLike);
    verify(feedLikeRepository, times(0)).save(any());
  }

  @Nested
  @DisplayName("피드 수정")
  class UpdateFeed {

    @Test
    @DisplayName("성공 : 작성자가 자신의 피드를 수정한다.")
    void updateFeedSuccess() {
      Long currentUserId = 1L;
      Long feedId = 10L;
      String newContent = "새롭게 수정된 내용";
      FeedUpdateRequest request = new FeedUpdateRequest(newContent);

      User mockAuthor = mock(User.class);
      Feed mockFeed = mock(Feed.class);

      when(feedRepository.findById(feedId)).thenReturn(Optional.of(mockFeed));
      when(mockFeed.getAuthor()).thenReturn(mockAuthor);
      when(mockAuthor.getId()).thenReturn(currentUserId);
      when(userRepository.findById(currentUserId)).thenReturn(Optional.of(mockAuthor));
      when(mockFeed.getFeedClothes()).thenReturn(Collections.emptyList());
      when(mockFeed.getContent()).thenReturn(newContent);

      // Mock 매퍼 응답 설정
      FeedDto mockFeedDto = mock(FeedDto.class);
      when(feedMapper.toDto(any(Feed.class), any(User.class), anyLong(), anyInt(), anyBoolean()))
          .thenReturn(mockFeedDto);

      FeedDto result = feedService.updateFeed(currentUserId, feedId, request);

      verify(mockFeed, times(1)).updateContent(newContent);
      verify(feedMapper, times(1)).toDto(any(Feed.class), any(User.class), anyLong(), anyInt(), anyBoolean());

    }

    @Test
    @DisplayName("실패 - 작성자가 아닌 사용자가 수정을 시도")
    void updatedFeedFail_Anotherperson() {
      Long currentUserId = 1L;
      Long authorId = 2L;
      Long feedId = 10L;
      FeedUpdateRequest request = new FeedUpdateRequest("작성자가 아닌 사람이 수정중");

      User mockAuthor = mock(User.class);
      Feed mockFeed = mock(Feed.class);

      when(feedRepository.findById(feedId)).thenReturn(Optional.of(mockFeed));
      when(mockFeed.getAuthor()).thenReturn(mockAuthor);
      when(mockAuthor.getId()).thenReturn(authorId);

      assertThatThrownBy(() -> feedService.updateFeed(currentUserId, feedId, request))
          .isInstanceOf(StyleMyClosetException.class)
          .extracting("errorCode")
          .isEqualTo(ErrorCode.FEED_UPDATE_FORBIDDEN);
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 피드를 수정하려고 한다")
    void updateFeedFail_FeedNotFound() {
      Long currentUserId = 1L;
      Long feedId = 999L;
      FeedUpdateRequest request = new FeedUpdateRequest("수정할 내용");

      when(feedRepository.findById(feedId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> feedService.updateFeed(currentUserId, feedId, request))
          .isInstanceOf(StyleMyClosetException.class)
          .extracting("errorCode")
          .isEqualTo(ErrorCode.FEED_NOT_FOUND);
    }
  }
}

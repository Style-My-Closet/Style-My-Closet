package com.stylemycloset.ootd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.stylemycloset.cloth.entity.Cloth;
import com.stylemycloset.cloth.entity.ClothingCategory;
import com.stylemycloset.cloth.entity.ClothingCategoryType;
import com.stylemycloset.cloth.repository.ClothRepository;
import com.stylemycloset.ootd.dto.FeedCreateRequest;
import com.stylemycloset.ootd.dto.FeedDto;
import com.stylemycloset.ootd.entity.Feed;
import com.stylemycloset.ootd.repo.FeedClothesRepository;
import com.stylemycloset.ootd.repo.FeedRepository;
import com.stylemycloset.ootd.repo.UserRepository;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.weather.repository.WeatherRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

  @Test
  @DisplayName("피드 생성 요청 시 FeedDto로 반환된다")
  void createFeed_success() {
    // 준비
    Long authorId = 1L;
    List<Long> clothesIds = List.of(101L, 102L);
    FeedCreateRequest request = new FeedCreateRequest(authorId, null, clothesIds, "테스트 피드 내용");

    // 가짜 모형
    User mockUser = mock(User.class);
    Cloth mockCloth1 = mock(Cloth.class);
    Cloth mockCloth2 = mock(Cloth.class);
    ClothingCategory mockCategory = mock(ClothingCategory.class);

    // 레포지토리 행동 정의
    when(userRepository.findById(authorId)).thenReturn(Optional.of(mockUser));
    when(clothRepository.findAllById(clothesIds)).thenReturn(List.of(mockCloth1, mockCloth2));
    when(feedRepository.save(any(Feed.class))).thenAnswer(invocation -> {
      Feed feed = invocation.getArgument(0);
      ReflectionTestUtils.setField(feed, "id", 1L);
      ReflectionTestUtils.setField(feed, "createdAt", Instant.now());
      ReflectionTestUtils.setField(feed, "updatedAt", Instant.now());
      return feed;
    });

    // MOCK 행동 정의
    when(mockUser.getId()).thenReturn(authorId);
    when(mockUser.getName()).thenReturn("테스트유저");
    when(mockCloth1.getCategory()).thenReturn(mockCategory);
    when(mockCloth2.getCategory()).thenReturn(mockCategory);
    when(mockCategory.getName()).thenReturn(ClothingCategoryType.TOP);

    FeedDto result = feedService.createFeed(request);

    // 검증
    assertThat(result).isNotNull();
  }
}

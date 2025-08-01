package com.stylemycloset.ootd.service;

import com.stylemycloset.cloth.entity.Cloth;
import com.stylemycloset.cloth.entity.ClothingCategory; // ✅ ClothingCategory 임포트
import com.stylemycloset.cloth.repo.ClothRepository;
import com.stylemycloset.ootd.dto.FeedCreateRequest;
import com.stylemycloset.ootd.dto.FeedDto;
import com.stylemycloset.ootd.entity.Feed;
import com.stylemycloset.ootd.repo.FeedClothesRepository;
import com.stylemycloset.ootd.repo.FeedRepository;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repo.UserRepository;
import com.stylemycloset.weather.repo.WeatherRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

  @Test
  @DisplayName("OOTD 피드 등록 성공 테스트")
  void createFeed_success() {
    // 준비
    Long authorId = 1L;
    List<Long> clothesIds = List.of(101L, 102L);
    FeedCreateRequest request = new FeedCreateRequest(authorId, null, clothesIds, "테스트 피드 내용");

    // 가짜 객체 생성
    User fakeUser = mock(User.class);
    Cloth fakeCloth1 = mock(Cloth.class);
    Cloth fakeCloth2 = mock(Cloth.class);
    ClothingCategory fakeCategory = mock(ClothingCategory.class); // ✅ 카테고리 모형도 생성

    // 🧠 Mockito의 핵심: "만약 ~라고 물어보면, ~라고 대답해줘!" 라고 가짜 객체들의 행동(대본)을 정의
    when(userRepository.findById(authorId)).thenReturn(Optional.of(fakeUser));
    when(clothRepository.findAllById(clothesIds)).thenReturn(List.of(fakeCloth1, fakeCloth2));
    when(feedRepository.save(any(Feed.class))).thenAnswer(invocation -> {
      // save 요청이 오면, 받은 Feed 객체에 가짜 ID를 부여해서 돌려주도록 설정
      Feed feed = invocation.getArgument(0);
      // ReflectionTestUtils.setField(feed, "id", 1L); // ID를 세팅하는 더 고급 방법도 있음
      return feed;
    });

    // 가짜 프로그래밍
    when(fakeUser.getId()).thenReturn(authorId);
    when(fakeUser.getName()).thenReturn("테스트유저");
    when(fakeCloth1.getCategory()).thenReturn(fakeCategory);
    when(fakeCloth2.getCategory()).thenReturn(fakeCategory);
    when(fakeCategory.getName()).thenReturn("TOP"); // 카테고리 이름을 "TOP"으로 대답하도록 설정

    // 실행
    FeedDto result = feedService.createFeed(request);

    // 검증
    assertThat(result).isNotNull();
    assertThat(result.content()).isEqualTo("테스트 피드 내용");
    assertThat(result.author().userId()).isEqualTo(authorId);
    assertThat(result.author().name()).isEqualTo("테스트유저");
    assertThat(result.ootds()).hasSize(2);
    assertThat(result.ootds().get(0).type().name()).isEqualTo("TOP");

    // 🧠 추가 검증: "특정 메서드가 정확히 몇 번 호출되었니?"
    verify(feedRepository, times(1)).save(any(Feed.class));
    verify(feedClothesRepository, times(1)).saveAll(any());
  }
}

package com.stylemycloset.follow.repository;

import com.stylemycloset.follow.entity.Follow;
import com.stylemycloset.follow.entity.QFollow;
import com.stylemycloset.follow.service.UserRepository;
import com.stylemycloset.testutil.IntegrationTestSupport;
import com.stylemycloset.user.entity.User;
import java.util.List;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

class FollowRepositoryTest extends IntegrationTestSupport {

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private FollowRepository followRepository;

  @BeforeEach
  void setUp() {
    userRepository.deleteAllInBatch();
    followRepository.deleteAllInBatch();
  }

  @DisplayName("팔로우 관계가 SoftDelete 되었으면 조회되지 않습니다.")
  @Test
  void test_SoftDelete() {
    // given
    User userA = userRepository.save(new User("a", "a"));
    User userB = userRepository.save(new User("b", "b"));
    Follow follow = new Follow(userB, userA);
    follow.softDelete();
    Follow savedFollow = followRepository.save(follow);

    // when
    boolean isFollowed = followRepository.existsActiveByFolloweeIdAndFollowerId(userB.getId(),
        userA.getId());

    // then
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(followRepository.findById(savedFollow.getId()))
          .isPresent()
          .get()
          .extracting(Follow::isSoftDeleted)
          .isEqualTo(true);
      softly.assertThat(isFollowed).isFalse();
    });
  }

  @DisplayName("사용자가 팔로잉 목록을 조회하면, 기본 정렬은 최근 생성 순서(내림차순)이다")
  @Test
  void defaultSortFieldAndDirection() {
    // given
    User userA = userRepository.save(new User("a", "a"));
    User userB = userRepository.save(new User("b", "b"));
    User userC = userRepository.save(new User("c", "c"));
    Follow followAtoB = followRepository.save(new Follow(userB, userA));
    Follow followAtoC = followRepository.save(new Follow(userC, userA));

    // when
    Slice<Follow> result = followRepository.findFollowingsByFollowerId(
        userA.getId(), null, null, 2, null, null, null
    );

    // then
    Sort.Order order = result.getPageable().getSort().iterator().next();
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(order.getProperty())
          .isEqualTo(QFollow.follow.createdAt.getMetadata().getName());
      softly.assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
      softly.assertThat(result.getContent()).extracting(Follow::getId)
          .containsExactly(followAtoC.getId(), followAtoB.getId());
    });
  }

  @DisplayName("팔로잉 목록이 ID와 생성일 기준으로 정렬된다")
  @ParameterizedTest
  @MethodSource("resolveFollowFixedField")
  void sortFollowings_ByIdAndCreatedAt_ReturnsCorrectOrder(String sortBy) {
    // given
    User userA = userRepository.save(new User("a", "a"));
    User userB = userRepository.save(new User("b", "b"));
    User userC = userRepository.save(new User("c", "c"));
    Follow followAtoB = followRepository.save(new Follow(userB, userA));
    Follow followAtoC = followRepository.save(new Follow(userC, userA));

    // when
    Slice<Follow> result = followRepository.findFollowingsByFollowerId(
        userA.getId(), null, null, 2, null, sortBy, "DESC"
    );

    // then
    Assertions.assertThat(result.getContent())
        .hasSize(2)
        .extracting(Follow::getId)
        .containsExactly(followAtoC.getId(), followAtoB.getId());
  }

  static Stream<Arguments> resolveFollowFixedField() {
    return Stream.of(
        Arguments.of(QFollow.follow.id.getMetadata().getName()),
        Arguments.of(QFollow.follow.createdAt.getMetadata().getName())
    );
  }

  @DisplayName("이름 필터(nameLike)와 요청 데이터 수(limit), 정렬방향(direction)을 조합해 팔로잉 목록을 조회한다")
  @ParameterizedTest(name = "[{index}] nameLike={0}, limit={1}, direction={2} -> 기대={3}, hasNext={4}")
  @MethodSource("nameFilterCases")
  void shouldFilterByNameAndSortDescWithLimit(
      String nameLike,
      int limit,
      String direction,
      List<String> expectedNames,
      boolean expectHasNext
  ) {
    // given
    User userA = userRepository.save(new User("a", "a"));
    User userB = userRepository.save(new User("bbb", "b"));
    User userC = userRepository.save(new User("ccc", "c"));
    User userD = userRepository.save(new User("abcd", "d"));

    followRepository.save(new Follow(userB, userA));
    followRepository.save(new Follow(userC, userA));
    followRepository.save(new Follow(userD, userA));

    // when
    Slice<Follow> result = followRepository.findFollowingsByFollowerId(
        userA.getId(),
        null,
        null,
        limit,
        nameLike,
        QFollow.follow.createdAt.getMetadata().getName(),
        direction
    );

    // then
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(result.getNumberOfElements()).isEqualTo(expectedNames.size());
      softly.assertThat(result.hasNext()).isEqualTo(expectHasNext);
      softly.assertThat(result.getContent())
          .extracting(f -> f.getFollowee().getName())
          .containsExactlyElementsOf(expectedNames);
    });
  }

  static Stream<Arguments> nameFilterCases() {
    return Stream.of(
        Arguments.of(null, 2, Direction.DESC.name(), List.of("abcd", "ccc"), true),
        Arguments.of("c", 2, Direction.DESC.name(), List.of("abcd", "ccc"), false),
        Arguments.of("b", 1, Direction.DESC.name(), List.of("abcd"), true),
        Arguments.of("c", 1, Direction.DESC.name(), List.of("abcd"), true),

        Arguments.of(null, 2, Direction.ASC.name(), List.of("bbb", "ccc"), true),
        Arguments.of("c", 2, Direction.ASC.name(), List.of("ccc", "abcd"), false),
        Arguments.of("b", 1, Direction.ASC.name(), List.of("bbb"), true),
        Arguments.of("c", 1, Direction.ASC.name(), List.of("ccc"), true)
    );
  }

  @DisplayName("사용자가 지정한 정렬 기준에 따라 복원된 팔로우까지 정렬된다")
  @ParameterizedTest
  @MethodSource("resolveFollowUpdatableField")
  void sortFollowings_WithRestoredFollow_ThenOrderIsCorrect(String sortBy) {
    // given
    User userA = userRepository.save(new User("a", "a"));
    User userB = userRepository.save(new User("b", "b"));
    User userC = userRepository.save(new User("c", "c"));
    Follow followAtoB = followRepository.save(new Follow(userB, userA));
    Follow followAtoC = followRepository.save(new Follow(userC, userA));
    followAtoB.softDelete();
    followAtoB.restore();
    followRepository.save(followAtoB);

    // when
    Slice<Follow> result = followRepository.findFollowingsByFollowerId(
        userA.getId(), null, null, 2, null, sortBy, "DESC"
    );

    // then
    Assertions.assertThat(result.getContent())
        .hasSize(2)
        .extracting(Follow::getId)
        .containsExactly(followAtoB.getId(), followAtoC.getId());
  }

  static Stream<Arguments> resolveFollowUpdatableField() {
    return Stream.of(
        Arguments.of(QFollow.follow.updatedAt.getMetadata().getName()),
        Arguments.of(QFollow.follow.followedAt.getMetadata().getName())
    );
  }

  @DisplayName("팔로우 관계가 SoftDelete 되었으면 조회 결과에 반영하지 않습니다.")
  @Test
  void test_SoftDelete_Follow() {
    // given
    User userA = userRepository.save(new User("a", "a"));
    User userB = userRepository.save(new User("b", "b"));
    User userC = userRepository.save(new User("c", "c"));
    Follow followAtoB = followRepository.save(new Follow(userB, userA));
    Follow followAtoC = followRepository.save(new Follow(userC, userA));

    followAtoC.softDelete();
    Follow savedFollow = followRepository.save(followAtoC);

    // when
    Slice<Follow> result = followRepository.findFollowingsByFollowerId(
        userA.getId(), null, null, 2, null, null, null
    );

    // then
    Sort.Order order = result.getPageable().getSort().iterator().next();
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(order.getProperty())
          .isEqualTo(QFollow.follow.createdAt.getMetadata().getName());
      softly.assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
      softly.assertThat(result.getContent()).extracting(Follow::getId)
          .containsExactly(followAtoB.getId());
    });
  }


  @DisplayName("팔로우 대상 조회시 대상이 SoftDelete 되었으면 조회 결과에 반영하지 않습니다.")
  @Test
  void test_SoftDelete_FollowerName() {
    // given
    User userA = userRepository.save(new User("a", "a"));
    User userB = userRepository.save(new User("b", "b"));
    User userC = userRepository.save(new User("c", "c"));
    Follow followAtoB = followRepository.save(new Follow(userB, userA));
    Follow followAtoC = followRepository.save(new Follow(userC, userA));

    userC.softDelete();
    userRepository.save(userC);

    // when
    Slice<Follow> result = followRepository.findFollowingsByFollowerId(
        userA.getId(), null, null, 2, null, null, null
    );

    // then
    Sort.Order order = result.getPageable().getSort().iterator().next();
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(order.getProperty())
          .isEqualTo(QFollow.follow.createdAt.getMetadata().getName());
      softly.assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
      softly.assertThat(result.getContent()).extracting(Follow::getId)
          .containsExactly(followAtoB.getId());
    });
  }

}
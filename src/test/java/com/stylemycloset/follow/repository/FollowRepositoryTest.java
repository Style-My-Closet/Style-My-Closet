package com.stylemycloset.follow.repository;

import static com.stylemycloset.follow.entity.QFollow.follow;

import com.stylemycloset.IntegrationTestSupport;
import com.stylemycloset.follow.entity.Follow;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import java.util.List;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
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

  @AfterEach
  void tearDown() {
    userRepository.deleteAllInBatch();
    followRepository.deleteAllInBatch();
  }

  @Test
  @DisplayName("유저 A의 팔로워 수와 팔로잉 수를 정확히 카운트한다")
  void countActiveFollowersAndFollowings() {
    // given
    User userA = userRepository.save(new User("A", "a@example.com", "P"));
    User userB = userRepository.save(new User("B", "b@example.com", "P"));
    User userC = userRepository.save(new User("C", "c@example.com", "P"));

    Follow followBtoA = followRepository.save(new Follow(userA, userB));
    Follow followAtoB = followRepository.save(new Follow(userB, userA));
    Follow followAtoC = followRepository.save(new Follow(userC, userA));

    // when
    long followersCount = followRepository.countActiveFollowers(userA.getId());
    long followingsCount = followRepository.countActiveFollowings(userA.getId());

    // then
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(followersCount).isEqualTo(1);
      softly.assertThat(followingsCount).isEqualTo(2);
    });
  }

  @DisplayName("팔로우 관계가 SoftDelete 되었으면 조회되지 않습니다.")
  @Test
  void test_SoftDelete() {
    // given
    User userA = userRepository.save(new User("a", "a", "p"));
    User userB = userRepository.save(new User("b", "b", "p"));
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
    User userA = userRepository.save(new User("a", "a", "p"));
    User userB = userRepository.save(new User("b", "b", "p"));
    User userC = userRepository.save(new User("c", "c", "p"));
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
          .isEqualTo(follow.createdAt.getMetadata().getName());
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
    User userA = userRepository.save(new User("a", "a", "p"));
    User userB = userRepository.save(new User("b", "b", "p"));
    User userC = userRepository.save(new User("c", "c", "p"));
    Follow followAtoB = followRepository.save(new Follow(userB, userA));
    Follow followAtoC = followRepository.save(new Follow(userC, userA));

    // when
    Slice<Follow> result = followRepository.findFollowingsByFollowerId(
        userA.getId(), null, null, 2, null, sortBy, Direction.DESC
    );

    // then
    Assertions.assertThat(result.getContent())
        .hasSize(2)
        .extracting(Follow::getId)
        .containsExactly(followAtoC.getId(), followAtoB.getId());
  }

  static Stream<Arguments> resolveFollowFixedField() {
    return Stream.of(
        Arguments.of(follow.id.getMetadata().getName()),
        Arguments.of(follow.createdAt.getMetadata().getName())
    );
  }

  @DisplayName("이름 필터(nameLike)와 요청 데이터 수(limit), 정렬방향(direction)을 조합해 팔로잉 목록을 조회한다")
  @ParameterizedTest(name = "[{index}] nameLike={0}, limit={1}, direction={2} -> 기대={3}, hasNext={4}")
  @MethodSource("nameFilterCases")
  void shouldFilterByNameAndSortDescWithLimit(
      String nameLike,
      int limit,
      Direction direction,
      List<String> expectedNames,
      boolean expectHasNext
  ) {
    // given
    User userA = userRepository.save(new User("a", "a", "p"));
    User userB = userRepository.save(new User("bbb", "b", "p"));
    User userC = userRepository.save(new User("ccc", "c", "p"));
    User userD = userRepository.save(new User("abcd", "d", "p"));

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
        follow.createdAt.getMetadata().getName(),
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
        Arguments.of(null, 2, Direction.DESC, List.of("abcd", "ccc"), true),
        Arguments.of("c", 2, Direction.DESC, List.of("abcd", "ccc"), false),
        Arguments.of("b", 1, Direction.DESC, List.of("abcd"), true),
        Arguments.of("c", 1, Direction.DESC, List.of("abcd"), true),

        Arguments.of(null, 2, Direction.ASC, List.of("bbb", "ccc"), true),
        Arguments.of("c", 2, Direction.ASC, List.of("ccc", "abcd"), false),
        Arguments.of("b", 1, Direction.ASC, List.of("bbb"), true),
        Arguments.of("c", 1, Direction.ASC, List.of("ccc"), true)
    );
  }

  @DisplayName("사용자가 지정한 정렬 기준에 따라 복원된 팔로우까지 정렬된다")
  @Test
  void sortFollowings_WithRestoredFollow_ThenOrderIsCorrect() {
    // given
    User userA = userRepository.save(new User("a", "a", "p"));
    User userB = userRepository.save(new User("b", "b", "p"));
    User userC = userRepository.save(new User("c", "c", "p"));
    Follow followAtoB = followRepository.save(new Follow(userB, userA));
    Follow followAtoC = followRepository.save(new Follow(userC, userA));
    followAtoB.softDelete();
    followAtoB.restore();
    followRepository.save(followAtoB);

    // when
    Slice<Follow> result = followRepository.findFollowingsByFollowerId(
        userA.getId(), null, null, 2, null, follow.followedAt.getMetadata().getName(), Direction.DESC
    );

    // then
    Assertions.assertThat(result.getContent())
        .hasSize(2)
        .extracting(Follow::getId)
        .containsExactly(followAtoB.getId(), followAtoC.getId());
  }

  @DisplayName("팔로우 관계가 SoftDelete 되었으면 조회 결과에 반영하지 않습니다.")
  @Test
  void test_SoftDelete_Follow() {
    // given
    User userA = userRepository.save(new User("a", "a", "p"));
    User userB = userRepository.save(new User("b", "b", "p"));
    User userC = userRepository.save(new User("c", "c", "p"));
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
          .isEqualTo(follow.createdAt.getMetadata().getName());
      softly.assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
      softly.assertThat(result.getContent()).extracting(Follow::getId)
          .containsExactly(followAtoB.getId());
    });
  }


  @DisplayName("팔로우 대상 조회시 대상이 SoftDelete 되었으면 조회 결과에 반영하지 않습니다.")
  @Test
  void test_SoftDelete_FollowerName() {
    // given
    User userA = userRepository.save(new User("a", "a", "p"));
    User userB = userRepository.save(new User("b", "b", "p"));
    User userC = userRepository.save(new User("c", "c", "p"));
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
          .isEqualTo(follow.createdAt.getMetadata().getName());
      softly.assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
      softly.assertThat(result.getContent()).extracting(Follow::getId)
          .containsExactly(followAtoB.getId());
    });
  }

  @DisplayName("팔로워 목록의 기본 정렬은 createdAt DESC 이다")
  @Test
  void defaultSortFollowers() {
    // given
    User userA = userRepository.save(new User("followee", "e", "p"));
    User userB = userRepository.save(new User("f1", "f1", "p"));
    User userC = userRepository.save(new User("f2", "f2", "p"));

    Follow followBtoA = followRepository.save(new Follow(userA, userB));
    Follow followCtoA = followRepository.save(new Follow(userA, userC));

    // when
    Slice<Follow> result = followRepository.findFollowersByFolloweeId(
        userA.getId(), null, null, 2, null, null, null
    );

    // then
    Sort.Order order = result.getPageable().getSort().iterator().next();
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(order.getProperty())
          .isEqualTo(follow.createdAt.getMetadata().getName());
      softly.assertThat(order.getDirection()).isEqualTo(Direction.DESC);
      softly.assertThat(result.getContent()).extracting(Follow::getId)
          .containsExactly(followCtoA.getId(), followBtoA.getId());
    });
  }

}
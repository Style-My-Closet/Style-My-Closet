package com.stylemycloset.follow.service.impl;

import com.stylemycloset.binarycontent.storage.BinaryContentStorage;
import com.stylemycloset.follow.dto.FollowListResponse;
import com.stylemycloset.follow.dto.FollowResult;
import com.stylemycloset.follow.dto.FollowSummaryResult;
import com.stylemycloset.follow.dto.request.FollowCreateRequest;
import com.stylemycloset.follow.dto.request.SearchFollowingsCondition;
import com.stylemycloset.follow.entity.Follow;
import com.stylemycloset.follow.exception.FollowAlreadyExist;
import com.stylemycloset.follow.exception.FollowSelfForbiddenException;
import com.stylemycloset.follow.repository.FollowRepository;
import com.stylemycloset.follow.service.FollowService;
import com.stylemycloset.follow.service.UserRepository;
import com.stylemycloset.testutil.IntegrationTestSupport;
import com.stylemycloset.user.entity.User;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class FollowServiceTest extends IntegrationTestSupport {

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private FollowRepository followRepository;
  @Autowired
  private FollowService followService;

  @MockitoBean
  private BinaryContentStorage binaryContentStorage;

  @BeforeEach
  void setUp() {
    userRepository.deleteAllInBatch();
    followRepository.deleteAllInBatch();
  }

  @DisplayName("A 유저가 B 유저를 팔로우한다")
  @Test
  void startFollowing() {
    // given
    User userA = userRepository.save(new User("a", "a"));
    User userB = userRepository.save(new User("b", "b"));
    FollowCreateRequest followCreateRequest = new FollowCreateRequest(userB.getId(), userA.getId());

    // when
    FollowResult followResult = followService.startFollowing(followCreateRequest);

    // then
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(followRepository.findAll()).hasSize(1);
      softly.assertThat(followResult.follower().userId()).isEqualTo(userA.getId());
      softly.assertThat(followResult.followee().userId()).isEqualTo(userB.getId());
    });
  }

  @DisplayName("자신은 스스로를 팔로우 할 수 없다")
  @Test
  void testForbiddenBySelf() {
    // given
    User userA = userRepository.save(new User("a", "a"));
    FollowCreateRequest followCreateRequest = new FollowCreateRequest(userA.getId(), userA.getId());

    // when & then
    Assertions.assertThatThrownBy(() -> followService.startFollowing(followCreateRequest))
        .isInstanceOf(FollowSelfForbiddenException.class);
  }

  @DisplayName("A 유저가 B 유저를 팔로우할떄 B 유저가 없으면, 팔로우 되지 않는다")
  @Test
  void testBNotPresent() {
    // given
    User userA = userRepository.save(new User("a", "a"));
    FollowCreateRequest followCreateRequest = new FollowCreateRequest(-1L, userA.getId());

    // when & then
    Assertions.assertThatThrownBy(() -> followService.startFollowing(followCreateRequest))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @DisplayName("A 유저가 B 유저를 팔로우 요청 할떄 이미 팔로잉하고 있으면, 팔로우되지 않는다")
  @Test
  void testFollowAlreadyExist() {
    // given
    User userA = userRepository.save(new User("a", "a"));
    User userB = userRepository.save(new User("b", "b"));
    followRepository.save(new Follow(userB, userA));
    FollowCreateRequest followCreateRequest = new FollowCreateRequest(userB.getId(), userA.getId());

    // when & then
    Assertions.assertThatThrownBy(() -> followService.startFollowing(followCreateRequest))
        .isInstanceOf(FollowAlreadyExist.class);
  }

  @DisplayName("A 유저가 B 유저를 팔로우 취소 후 팔로우를 하면, 새로 생성되지 않고 이전 팔로우 기록이 복구된다")
  @Test
  void testFollowSoftDeleteRestore() {
    // given
    User userA = userRepository.save(new User("a", "a"));
    User userB = userRepository.save(new User("b", "b"));
    Follow follow = followRepository.save(new Follow(userB, userA));
    follow.softDelete();
    Follow updatedFollow = followRepository.save(follow);
    FollowCreateRequest followCreateRequest = new FollowCreateRequest(userB.getId(), userA.getId());

    // when
    FollowResult followResult = followService.startFollowing(followCreateRequest);

    // then
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(followRepository.findAll())
          .hasSize(1)
          .extracting(Follow::getDeletedAt)
          .containsOnlyNulls();
      softly.assertThat(followResult)
          .extracting(
              testFollowResult -> testFollowResult.followee().userId(),
              testFollowResult -> testFollowResult.follower().userId(),
              FollowResult::id
          ).containsExactly(userB.getId(), userA.getId(), updatedFollow.getId());
    });
  }

  @DisplayName("유저 A가 유저 B를 팔로우하면, 팔로우 요약 정보에 해당 팔로우 정보가 포함되어야 한다")
  @Test
  void summaryFollowInfo() {
    // given
    User userA = userRepository.save(new User("a", "a"));
    User userB = userRepository.save(new User("b", "b"));
    Follow savedFollow = followRepository.save(new Follow(userB, userA));

    // when
    FollowSummaryResult followSummary = followService.summaryFollow(userB.getId(), userA.getId());

    // then
    Assertions.assertThat(followSummary)
        .extracting(
            FollowSummaryResult::followedByMe, FollowSummaryResult::followedByMeId,
            FollowSummaryResult::followeeId, FollowSummaryResult::followerCount,
            FollowSummaryResult::followingCount
        ).containsExactly(true, savedFollow.getId(), userB.getId(), 1L, 0L);
  }

  @DisplayName("유저 A가 유저 B를 팔로우하지 않으면, 팔로우 요약 정보에 해당 팔로우 정보가 포함되어야 한다")
  @Test
  void summaryFollow_NoneFollow() {
    // given
    User userA = userRepository.save(new User("a", "a"));
    User userB = userRepository.save(new User("b", "b"));

    // when
    FollowSummaryResult followSummary = followService.summaryFollow(userB.getId(), userA.getId());

    // then
    Assertions.assertThat(followSummary)
        .extracting(
            FollowSummaryResult::followedByMe, FollowSummaryResult::followedByMeId,
            FollowSummaryResult::followeeId, FollowSummaryResult::followerCount,
            FollowSummaryResult::followingCount
        ).containsExactly(false, null, userB.getId(), 0L, 0L);
  }

  @DisplayName("사용자가 팔로잉 목록을 첫 페이지 이후로 조회하면, 중복 없이 이어서 조회된다")
  @Test
  void cursorPagination_nextPage_noOverlap() {
    // given
    User userA = userRepository.save(new User("a", "a"));
    User userB = userRepository.save(new User("b", "b"));
    User userC = userRepository.save(new User("c", "c"));
    Follow followAtoB = followRepository.save(new Follow(userB, userA));
    Follow followAtoC = followRepository.save(new Follow(userC, userA));

    FollowListResponse<FollowResult> firstSearchResult = followService.getFollowings(
        new SearchFollowingsCondition(userA.getId(), null, null, 1, null, null, "DESC")
    );

    // when
    FollowListResponse<FollowResult> secondSearchResult = followService.getFollowings(
        new SearchFollowingsCondition(
            userA.getId(),
            firstSearchResult.nextCursor(),
            firstSearchResult.nextIdAfter(),
            1,
            null,
            null,
            "DESC")
    );

    // then
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(secondSearchResult.hasNext()).isFalse();
      softly.assertThat(secondSearchResult.data())
          .extracting(FollowResult::id)
          .containsExactly(followAtoB.getId());
    });
  }


  @Test
  void getFollowers() {

  }

  @Test
  void delete() {

  }

}
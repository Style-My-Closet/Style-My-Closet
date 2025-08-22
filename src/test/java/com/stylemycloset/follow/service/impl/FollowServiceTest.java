package com.stylemycloset.follow.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.stylemycloset.binarycontent.storage.s3.BinaryContentStorage;
import com.stylemycloset.follow.dto.FollowListResponse;
import com.stylemycloset.follow.dto.FollowResult;
import com.stylemycloset.follow.dto.FollowSummaryResult;
import com.stylemycloset.follow.dto.request.FollowCreateRequest;
import com.stylemycloset.follow.dto.request.SearchFollowersCondition;
import com.stylemycloset.follow.dto.request.SearchFollowingsCondition;
import com.stylemycloset.follow.entity.Follow;
import com.stylemycloset.follow.entity.QFollow;
import com.stylemycloset.follow.exception.ActiveFollowNotFoundException;
import com.stylemycloset.follow.exception.FollowAlreadyExistException;
import com.stylemycloset.follow.exception.FollowNotFoundException;
import com.stylemycloset.follow.exception.FollowSelfForbiddenException;
import com.stylemycloset.follow.repository.FollowRepository;
import com.stylemycloset.follow.service.FollowService;
import com.stylemycloset.IntegrationTestSupport;
import com.stylemycloset.notification.dto.NotificationDto;
import com.stylemycloset.sse.service.SseService;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;

class FollowServiceTest extends IntegrationTestSupport {

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private FollowRepository followRepository;
  @Autowired
  private FollowService followService;

  @MockitoBean
  private BinaryContentStorage binaryContentStorage;
  @MockitoBean
  private SseService sseService;

  @AfterEach
  void tearDown() {
    userRepository.deleteAllInBatch();
    followRepository.deleteAllInBatch();
  }

  @DisplayName("A 유저가 B 유저를 팔로우한다")
  @Test
  void startFollowing() {
    // given
    User userA = userRepository.save(new User("a", "a", "a"));
    User userB = userRepository.save(new User("b", "b", "b"));
    FollowCreateRequest followCreateRequest = new FollowCreateRequest(userB.getId(), userA.getId());

    // when
    FollowResult followResult = followService.startFollowing(followCreateRequest);

    if (TestTransaction.isActive()) {
      TestTransaction.flagForCommit();
      TestTransaction.end();
    }

    // then
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(followRepository.findAll()).hasSize(1);
      softly.assertThat(followResult.follower().userId()).isEqualTo(userA.getId());
      softly.assertThat(followResult.followee().userId()).isEqualTo(userB.getId());
    });

    verify(sseService).sendNotification(any(NotificationDto.class));
  }

  @DisplayName("자신은 스스로를 팔로우 할 수 없다")
  @Test
  void testForbiddenBySelf() {
    // given
    User userA = userRepository.save(new User("a", "a", "a"));
    FollowCreateRequest followCreateRequest = new FollowCreateRequest(userA.getId(), userA.getId());

    // when & then
    Assertions.assertThatThrownBy(() -> followService.startFollowing(followCreateRequest))
        .isInstanceOf(FollowSelfForbiddenException.class);
  }

  @DisplayName("A 유저가 B 유저를 팔로우할떄 B 유저가 없으면, 팔로우 되지 않는다")
  @Test
  void testBNotPresent() {
    // given
    User userA = userRepository.save(new User("a", "a", "a"));
    FollowCreateRequest followCreateRequest = new FollowCreateRequest(-1L, userA.getId());

    // when & then
    Assertions.assertThatThrownBy(() -> followService.startFollowing(followCreateRequest))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @DisplayName("A 유저가 B 유저를 팔로우 요청 할떄 이미 팔로잉하고 있으면, 팔로우되지 않는다")
  @Test
  void testFollowAlreadyExist() {
    // given
    User userA = userRepository.save(new User("a", "a", "a"));
    User userB = userRepository.save(new User("b", "b", "b"));
    followRepository.save(new Follow(userB, userA));
    FollowCreateRequest followCreateRequest = new FollowCreateRequest(userB.getId(), userA.getId());

    // when & then
    Assertions.assertThatThrownBy(() -> followService.startFollowing(followCreateRequest))
        .isInstanceOf(FollowAlreadyExistException.class);
  }

  @DisplayName("A 유저가 B 유저를 팔로우 취소 후 팔로우를 하면, 새로 생성되지 않고 이전 팔로우 기록이 복구된다")
  @Test
  void testFollowSoftDeleteRestore() {
    // given
    User userA = userRepository.save(new User("a", "a", "a"));
    User userB = userRepository.save(new User("b", "b", "b"));
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

  @DisplayName("로그인 유저가 다른 유저의 팔로우 요약 정보를 조회하면 팔로우 여부가 포함된다")
  @Test
  void summaryFollowInfo() {
    // given
    User userA = userRepository.save(new User("a", "a", "a"));
    User userB = userRepository.save(new User("b", "b", "b"));
    User userC = userRepository.save(new User("c", "c", "c"));
    Follow followAtoB = followRepository.save(new Follow(userB, userA));
    Follow followAtoC = followRepository.save(new Follow(userC, userA));
    Follow followCtoA = followRepository.save(new Follow(userA, userC));

    // when
    FollowSummaryResult followSummary = followService.getFollowSummary(
        userA.getId(),
        userC.getId()
    );

    // then
    Assertions.assertThat(followSummary)
        .extracting(
            FollowSummaryResult::followeeId,
            FollowSummaryResult::followerCount,
            FollowSummaryResult::followingCount,
            FollowSummaryResult::followedByMe,
            FollowSummaryResult::followedByMeId,
            FollowSummaryResult::followingMe)
        .containsExactly(userA.getId(), 1L, 2L, true, followCtoA.getId(), true);
  }

  @DisplayName("로그인 유저가 다른 유저의 팔로우 요약 정보를 조회하면 팔로우 여부가 포함된다(팔로우 정보가 없을떄)")
  @Test
  void summaryFollow_NoneFollow() {
    // given
    User userA = userRepository.save(new User("a", "a", "a"));
    User userB = userRepository.save(new User("b", "b", "b"));

    // when
    FollowSummaryResult followSummary = followService.getFollowSummary(
        userB.getId(),
        userA.getId()
    );

    // then
    Assertions.assertThat(followSummary)
        .extracting(
            FollowSummaryResult::followeeId,
            FollowSummaryResult::followerCount,
            FollowSummaryResult::followingCount,
            FollowSummaryResult::followedByMe,
            FollowSummaryResult::followedByMeId,
            FollowSummaryResult::followingMe)
        .containsExactly(userB.getId(), 0L, 0L, false, null, false);
  }

  @DisplayName("사용자가 팔로워의 팔로잉 목록을 첫 페이지 이후로 조회하면, 중복 없이 이어서 조회된다")
  @Test
  void cursorPagination_nextPage_noOverlap() {
    // given
    User userA = userRepository.save(new User("a", "a", "a"));
    User userB = userRepository.save(new User("b", "b", "b"));
    User userC = userRepository.save(new User("c", "c", "c"));
    Follow followAtoB = followRepository.save(new Follow(userB, userA));
    Follow followAtoC = followRepository.save(new Follow(userC, userA));

    FollowListResponse<FollowResult> firstSearchResult = followService.getFollowings(
        new SearchFollowingsCondition(userA.getId(), null, null, 1, null,
            QFollow.follow.createdAt.getMetadata().getName(), "DESC")
    );

    // when
    FollowListResponse<FollowResult> secondSearchResult = followService.getFollowings(
        new SearchFollowingsCondition(
            userA.getId(),
            firstSearchResult.nextCursor(),
            firstSearchResult.nextIdAfter(),
            1,
            null,
            QFollow.follow.createdAt.getMetadata().getName(),
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

  @DisplayName("팔로워 목록을 다음 페이지로 조회하면 이전 페이지와 중복 없이 이어진다")
  @Test
  void cursorPagination_followers_nextPage_noOverlap() {
    // given
    User userA = userRepository.save(new User("a", "a", "a"));
    User userB = userRepository.save(new User("b", "b", "b"));
    User userC = userRepository.save(new User("c", "c", "c"));
    Follow followBtoA = followRepository.save(new Follow(userA, userB));
    Follow followCtoA = followRepository.save(new Follow(userA, userC));

    FollowListResponse<FollowResult> firstSearchResult = followService.getFollowers(
        new SearchFollowersCondition(userA.getId(), null, null, 1, null,
            QFollow.follow.createdAt.getMetadata().getName(), "DESC")
    );

    // when
    FollowListResponse<FollowResult> secondSearchResult = followService.getFollowers(
        new SearchFollowersCondition(
            userA.getId(),
            firstSearchResult.nextCursor(),
            firstSearchResult.nextIdAfter(),
            1,
            null,
            QFollow.follow.createdAt.getMetadata().getName(),
            "DESC"
        )
    );

    // then
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(secondSearchResult.hasNext()).isFalse();
      softly.assertThat(secondSearchResult.data())
          .extracting(FollowResult::id)
          .containsExactly(followBtoA.getId());
    });
  }

  @DisplayName("팔로우 관계를 소프트 삭제하면, 삭제 상태로 표시되고 활성 상태 조회에서 제외된다")
  @Test
  void softDelete_marksDeleted_and_excludedFromActiveSearch() {
    // given
    User followee = userRepository.save(new User("followee", "e", "p"));
    User follower = userRepository.save(new User("follower", "f", "p"));
    Follow follow = followRepository.save(new Follow(followee, follower));

    // when
    followService.softDelete(follow.getId());

    // then
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(followRepository.findById(follow.getId()))
          .isPresent()
          .get()
          .satisfies(f -> {
            softly.assertThat(f.isSoftDeleted()).isTrue();
            softly.assertThat(f.getDeletedAt()).isNotNull();
          });
      softly.assertThat(followRepository.existsActiveByFolloweeIdAndFollowerId(followee.getId(),
          follower.getId())).isFalse();
    });
  }

  @DisplayName("이미 소프트 삭제된 팔로우 관계를 다시 소프트 삭제하면, 활성 관계를 찾지 못해 예외가 발생한다")
  @Test
  void softDelete_whenAlreadyDeleted_throwsActiveFollowNotFound() {
    // given
    User followee = userRepository.save(new User("followee", "e", "p"));
    User follower = userRepository.save(new User("follower", "f", "p"));
    Follow follow = followRepository.save(new Follow(followee, follower));
    followService.softDelete(follow.getId());

    // when & then
    Assertions.assertThatThrownBy(() -> followService.softDelete(follow.getId()))
        .isInstanceOf(ActiveFollowNotFoundException.class);
  }

}
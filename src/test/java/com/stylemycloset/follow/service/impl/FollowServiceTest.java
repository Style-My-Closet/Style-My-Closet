package com.stylemycloset.follow.service.impl;

import com.stylemycloset.binarycontent.storage.BinaryContentStorage;
import com.stylemycloset.follow.dto.FollowResult;
import com.stylemycloset.follow.dto.request.FollowCreateRequest;
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

  @DisplayName("A 유저가 B 유저를 팔로우합니다.")
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

  @DisplayName("자기자신을 팔로우 할 수 없습니다.")
  @Test
  void testForbiddenBySelf() {
    // given
    User userA = userRepository.save(new User("a", "a"));
    FollowCreateRequest followCreateRequest = new FollowCreateRequest(userA.getId(), userA.getId());

    // when & then
    Assertions.assertThatThrownBy(() -> followService.startFollowing(followCreateRequest))
        .isInstanceOf(FollowSelfForbiddenException.class);
  }

  @DisplayName("A 유저가 B 유저를 팔로우할떄 B유저가 없으면, 팔로우 되지 않습니다.")
  @Test
  void testBNotPresent() {
    // given
    User userA = userRepository.save(new User("a", "a"));
    FollowCreateRequest followCreateRequest = new FollowCreateRequest(userA.getId(), userA.getId());

    // when & then
    Assertions.assertThatThrownBy(() -> followService.startFollowing(followCreateRequest))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @DisplayName("A 유저가 B 유저를 팔로우 요청 할떄 이미 팔로잉하고 있으면, 팔로우되지 않습니다.")
  @Test
  void testFollowAlreadyExist() {
    // given
    User userA = userRepository.save(new User("a", "a"));
    User userB = userRepository.save(new User("b", "b"));
    followRepository.save(new Follow(userA, userB));
    FollowCreateRequest followCreateRequest = new FollowCreateRequest(userA.getId(), userB.getId());

    // when & then
    Assertions.assertThatThrownBy(() -> followService.startFollowing(followCreateRequest))
        .isInstanceOf(FollowAlreadyExist.class);
  }

  @DisplayName("A 유저가 B 유저를 팔로우 취소 후 팔로우를 하면, 새로 생성되지 않고 이전 팔로우 기록이 복구됩니다.")
  @Test
  void testFollowSoftDeleteRestore() {
    // given
    User userA = userRepository.save(new User("a", "a"));
    User userB = userRepository.save(new User("b", "b"));
    Follow follow = followRepository.save(new Follow(userA, userB));
    follow.softDelete();
    Follow updatedFollow = followRepository.save(follow);
    FollowCreateRequest followCreateRequest = new FollowCreateRequest(userA.getId(), userB.getId());

    // when
    FollowResult followResult = followService.startFollowing(followCreateRequest);

    // then
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(followRepository.findAll())
          .hasSize(1)
          .extracting(Follow::getDeletedAt)
          .containsOnlyNulls();
      softly.assertThat(followResult.id()).isEqualTo(updatedFollow.getId());
    });
  }

  @Test
  void summaryFollowInfo() {
  }

  @Test
  void getFollowings() {
  }

  @Test
  void getFollowers() {
  }

  @Test
  void delete() {
  }

}
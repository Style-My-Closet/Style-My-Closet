package com.stylemycloset.follow.repository;

import com.stylemycloset.follow.entity.Follow;
import com.stylemycloset.follow.service.UserRepository;
import com.stylemycloset.testutil.IntegrationTestSupport;
import com.stylemycloset.user.entity.User;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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

  @DisplayName("팔로우 관계가 SoftDelete 되었으면 false를 반환한다")
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
      softly.assertThat(followRepository.findAll()).hasSize(1);
      softly.assertThat(isFollowed).isFalse();
      softly.assertThat(savedFollow)
          .extracting(Follow::getFollowee, Follow::getFollower)
          .containsExactly(userB, userA);
    });
  }

}
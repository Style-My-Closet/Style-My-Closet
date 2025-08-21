package com.stylemycloset.follow.mapper;

import com.stylemycloset.IntegrationTestSupport;
import com.stylemycloset.binarycontent.storage.s3.BinaryContentStorage;
import com.stylemycloset.follow.dto.FollowListResponse;
import com.stylemycloset.follow.dto.FollowResult;
import com.stylemycloset.follow.entity.Follow;
import com.stylemycloset.follow.repository.FollowRepository;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class FollowMapperTest extends IntegrationTestSupport {

  @MockitoBean
  private BinaryContentStorage binaryContentStorage;

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private FollowRepository followRepository;
  @Autowired
  private FollowMapper followMapper;

  @AfterEach
  void tearDown() {
    userRepository.deleteAllInBatch();
    followRepository.deleteAllInBatch();
  }

  @DisplayName("DESC: createdAt DESC 정렬, 다음 페이지 커서는 마지막 요소 기준")
  @Test
  void createdAtDesc_firstPage() {
    // given
    User userA = userRepository.save(new User("a", "a", "a"));
    User userB = userRepository.save(new User("b", "b", "b"));
    User userC = userRepository.save(new User("c", "c", "c"));

    Follow followAtoB = followRepository.save(new Follow(userB, userA));
    Follow followAtoC = followRepository.save(new Follow(userC, userA));

    Slice<Follow> slice = followRepository.findFollowingsByFollowerId(
        userA.getId(),
        null,
        null,
        2,
        null,
        null,
        null
    );

    // when
    FollowListResponse<FollowResult> result = followMapper.toFollowResponse(slice);

    // then
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(result.data())
          .extracting(FollowResult::id)
          .containsExactly(followAtoC.getId(), followAtoB.getId());
      softly.assertThat(result)
          .extracting(
              FollowListResponse::nextCursor,
              FollowListResponse::nextIdAfter
          ).containsExactly(
              null, null
          );
    });
  }

  @DisplayName("예외: Pageable이 unsorted일 때(getOrder가 첫 정렬을 못 찾아) NoSuchElementException")
  @Test
  void unsorted_pageable_throws() {
    // given
    User userA = userRepository.save(new User("unsA_it", "unsA_it", "unsA_it"));
    User userB = userRepository.save(new User("unsB_it", "unsB_it", "unsB_it"));
    User userC = userRepository.save(new User("c", "c", "c"));

    Follow followAtoB = followRepository.save(new Follow(userB, userA));
    Follow followAtoC = followRepository.save(new Follow(userC, userA));

    Slice<Follow> fetched = followRepository.findFollowingsByFollowerId(
        userA.getId(),
        null,
        null,
        2,
        null,
        "createdAt",
        "DESC"
    );

    Pageable unsorted = PageRequest.of(0, 2, Sort.unsorted());
    Slice<Follow> unsortedSlice = new SliceImpl<>(fetched.getContent(), unsorted, false);

    // when & then
    Assertions.assertThatThrownBy(() -> followMapper.toFollowResponse(unsortedSlice))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @DisplayName("예외: sort를 아예 지정하지 않은 Pageable(PageRequest.of(page,size))도 동일하게 실패")
  @Test
  void no_sort_specified_throws() {
    // given
    User userA = userRepository.save(new User("noSortA", "noSortA", "noSortA"));
    User userB = userRepository.save(new User("noSortB", "noSortB", "noSortB"));
    User userC = userRepository.save(new User("c", "c", "c"));

    Follow followAtoB = followRepository.save(new Follow(userB, userA));
    Follow followAtoC = followRepository.save(new Follow(userC, userA));

    Slice<Follow> fetched = followRepository.findFollowingsByFollowerId(
        userA.getId(),
        null,
        null,
        2,
        null,
        "createdAt",
        "DESC"
    );

    Pageable noSort = PageRequest.of(0, 2);
    Slice<Follow> sliceNoSort = new SliceImpl<>(fetched.getContent(), noSort, true);

    // when & then
    Assertions.assertThatThrownBy(() -> followMapper.toFollowResponse(sliceNoSort))
        .isInstanceOf(IllegalArgumentException.class);
  }

}
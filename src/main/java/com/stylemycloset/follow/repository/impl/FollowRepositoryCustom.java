package com.stylemycloset.follow.repository.impl;

import com.stylemycloset.follow.entity.Follow;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort.Direction;

public interface FollowRepositoryCustom {

  Slice<Follow> findFollowingsByFollowerId(
      Long followerId,
      String cursor,
      String idAfter,
      Integer limit,
      String nameLike,
      String sortBy,
      Direction direction
  );

  Slice<Follow> findFollowersByFolloweeId(
      Long followerId,
      String cursor,
      String idAfter,
      Integer limit,
      String nameLike,
      String sortBy,
      Direction direction
  );

}

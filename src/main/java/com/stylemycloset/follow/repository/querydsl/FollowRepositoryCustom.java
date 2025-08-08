package com.stylemycloset.follow.repository.querydsl;

import com.stylemycloset.follow.entity.Follow;
import org.springframework.data.domain.Slice;

public interface FollowRepositoryCustom {

  Slice<Follow> findFollowingsByFollowerId(
      Long followerId,
      String cursor,
      String idAfter,
      Integer limit,
      String nameLike,
      String sortBy,
      String sortDirection
  );

}

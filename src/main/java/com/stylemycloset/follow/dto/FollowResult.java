package com.stylemycloset.follow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stylemycloset.follow.entity.Follow;
import java.util.Objects;

public record FollowResult(
    Long id,
    FollowUserInfo followee,
    FollowUserInfo follower
) {

  public static FollowResult from(Follow follow, FollowUserInfo followee, FollowUserInfo follower) {
    return new FollowResult(
        Objects.requireNonNull(follow.getId()),
        followee,
        follower
    );
  }

}

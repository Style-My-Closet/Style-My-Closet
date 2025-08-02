package com.stylemycloset.follow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stylemycloset.follow.entity.Follow;

public record FollowResult(
    @JsonProperty("id")
    Long id,
    @JsonProperty("followee")
    FollowUserInfo followee,
    @JsonProperty("follower")
    FollowUserInfo follower
) {

  public static FollowResult from(Follow follow, FollowUserInfo followee, FollowUserInfo follower) {
    return new FollowResult(
        follow.getId(),
        followee,
        follower
    );
  }

}

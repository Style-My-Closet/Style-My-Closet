package com.stylemycloset.follow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stylemycloset.user.entity.User;

public record FollowUserInfo(
    @JsonProperty("userId")
    Long userId,
    @JsonProperty("name")
    String name,
    @JsonProperty("profileImageUrl")
    String profileImageUrl
) {

  public static FollowUserInfo of(User user, String profileImageUrl) {
    return new FollowUserInfo(
        user.getId(),
        user.getName(),
        profileImageUrl
    );
  }

}

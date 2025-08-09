package com.stylemycloset.follow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stylemycloset.user.entity.User;

public record FollowUserInfo(
    Long userId,
    String name,
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

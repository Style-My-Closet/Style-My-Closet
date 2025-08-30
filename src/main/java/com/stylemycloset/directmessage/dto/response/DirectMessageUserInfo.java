package com.stylemycloset.directmessage.dto.response;

import com.stylemycloset.user.entity.User;

public record DirectMessageUserInfo(
    Long id,
    String name,
    String profileImageUrl
) {

  public static DirectMessageUserInfo from(User user, String profileImageUrl) {
    return new DirectMessageUserInfo(
        user.getId(),
        user.getName(),
        profileImageUrl
    );
  }

}

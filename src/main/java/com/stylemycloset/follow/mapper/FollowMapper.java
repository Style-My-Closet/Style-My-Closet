package com.stylemycloset.follow.mapper;

import com.stylemycloset.binarycontent.storage.BinaryContentStorage;
import com.stylemycloset.follow.dto.FollowResult;
import com.stylemycloset.follow.dto.FollowUserInfo;
import com.stylemycloset.follow.entity.Follow;
import com.stylemycloset.user.entity.User;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FollowMapper {

  private final BinaryContentStorage binaryContentStorage;

  public FollowResult toResult(Follow follow) {
    User followee = follow.getFollowee();
    User follower = follow.getFollower();

    return FollowResult.from(
        follow,
        FollowUserInfo.of(followee, getProfileImageURL(followee)),
        FollowUserInfo.of(follower, getProfileImageURL(follower))
    );
  }

  private String getProfileImageURL(User user) {
    if (user.getProfileImage() == null) {
      return null;
    }
    UUID imageId = user.getProfileImage()
        .getId();

    return binaryContentStorage.getUrl(imageId)
        .toString();
  }

}

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
    FollowUserInfo followeeInfo = convertToFollowUserInfo(follow.getFollowee());
    FollowUserInfo followerInfo = convertToFollowUserInfo(follow.getFollower());

    return FollowResult.from(follow, followeeInfo, followerInfo);
  }

  private FollowUserInfo convertToFollowUserInfo(User user) {
    String profileImageURL = getProfileImageURL(user);

    return FollowUserInfo.of(user, profileImageURL);
  }

  private String getProfileImageURL(User user) {
    UUID imageId = convertProfileImageId(user);

    return binaryContentStorage.getUrl(imageId).toString();
  }

  private UUID convertProfileImageId(User user) {
    if (user.getProfileImage() == null) {
      return null;
    }
    return user.getProfileImage().getId();
  }

}

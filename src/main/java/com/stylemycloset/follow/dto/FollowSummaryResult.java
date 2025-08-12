package com.stylemycloset.follow.dto;

import com.stylemycloset.follow.entity.Follow;

public record FollowSummaryResult(
    Long followeeId,
    Long followerCount,
    Long followingCount,
    Boolean followedByMe,
    Long followedByMeId,
    Boolean followingMe
) {

  public static FollowSummaryResult of(
      Long targetUserId,
      Long followersNumber,
      Long followingsNumber,
      Follow logInUserFollowTargetUser,
      boolean isFollowingMe
  ) {
    return new FollowSummaryResult(
        targetUserId,
        followersNumber,
        followingsNumber,
        getFollowedByMe(logInUserFollowTargetUser),
        getFollowedByMeId(logInUserFollowTargetUser),
        isFollowingMe
    );
  }

  private static Boolean getFollowedByMe(Follow logInUserFollowTargetUser) {
    return null != logInUserFollowTargetUser;
  }

  private static Long getFollowedByMeId(Follow logInUserFollowTargetUser) {
    if (logInUserFollowTargetUser == null) {
      return null;
    }
    return logInUserFollowTargetUser.getId();
  }

}

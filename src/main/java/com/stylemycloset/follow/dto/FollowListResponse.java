package com.stylemycloset.follow.dto;

import java.util.List;

public record FollowListResponse<T>(
    List<T> data,
    String nextCursor,
    String nextIdAfter,
    Boolean hasNext,
    Integer totalCount,
    String sortBy,
    String sortDirection
) {

  public static FollowListResponse<FollowResult> of(
      List<FollowResult> followResults,
      NextCursorInfo nextCursorInfo,
      Boolean hasNext,
      Integer totalCount,
      String sortBy,
      String sortDirection
  ) {
    return new FollowListResponse<>(
        followResults,
        nextCursorInfo.nextCursor,
        nextCursorInfo.nextIdAfter,
        hasNext,
        totalCount,
        sortBy,
        sortDirection
    );
  }

  public record NextCursorInfo(String nextCursor, String nextIdAfter) {

  }

}

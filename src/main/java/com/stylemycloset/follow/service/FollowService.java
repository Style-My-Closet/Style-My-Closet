package com.stylemycloset.follow.service;

import com.stylemycloset.follow.dto.FollowListResponse;
import com.stylemycloset.follow.dto.FollowResult;
import com.stylemycloset.follow.dto.FollowSummaryResult;
import com.stylemycloset.follow.dto.request.FollowCreateRequest;
import com.stylemycloset.follow.dto.request.SearchFollowersCondition;
import com.stylemycloset.follow.dto.request.SearchFollowingsCondition;

public interface FollowService {

  FollowResult startFollowing(FollowCreateRequest followCreateRequest);

  FollowSummaryResult getFollowSummary(Long userId, Long viewerId);

  FollowListResponse<FollowResult> getFollowings(SearchFollowingsCondition followingsCondition);

  FollowListResponse<FollowResult> getFollowers(SearchFollowersCondition followersCondition);

  void softDelete(Long followId);

  void hardDelete(Long followId);

}

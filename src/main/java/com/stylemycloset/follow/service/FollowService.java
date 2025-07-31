package com.stylemycloset.follow.service;

import com.stylemycloset.follow.dto.FollowListResponse;
import com.stylemycloset.follow.dto.FollowResult;
import com.stylemycloset.follow.dto.FollowSummaryResult;
import com.stylemycloset.follow.dto.request.FollowCreateRequest;
import com.stylemycloset.follow.dto.request.SearchFollowersCondition;
import com.stylemycloset.follow.dto.request.SearchFollowingsCondition;

public interface FollowService {

  FollowResult startFollowing(FollowCreateRequest followCreateRequest);

  FollowSummaryResult summaryFollowInfo(Long userId);

  FollowListResponse<FollowResult> getFollowings(SearchFollowingsCondition followingsCondition);

  FollowListResponse<FollowResult> getFollowers(SearchFollowersCondition followersCondition);

  void delete(Long followId);

}

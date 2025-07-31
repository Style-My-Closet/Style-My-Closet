package com.stylemycloset.follow.service.impl;

import com.stylemycloset.follow.dto.FollowListResponse;
import com.stylemycloset.follow.dto.FollowResult;
import com.stylemycloset.follow.dto.FollowSummaryResult;
import com.stylemycloset.follow.dto.request.FollowCreateRequest;
import com.stylemycloset.follow.dto.request.SearchFollowersCondition;
import com.stylemycloset.follow.dto.request.SearchFollowingsCondition;
import com.stylemycloset.follow.repository.FollowRepository;
import com.stylemycloset.follow.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

  private final FollowRepository followRepository;

  @Override
  public FollowResult startFollowing(FollowCreateRequest followCreateRequest) {
    return null;
  }

  @Override
  public FollowSummaryResult summaryFollowInfo(Long userId) {
    return null;
  }

  @Override
  public FollowListResponse<FollowResult> getFollowings(
      SearchFollowingsCondition followingsCondition) {
    return null;
  }

  @Override
  public FollowListResponse<FollowResult> getFollowers(
      SearchFollowersCondition followersCondition) {
    return null;
  }

  @Override
  public void delete(Long followId) {

  }

}

package com.stylemycloset.follow.service.impl;

import com.stylemycloset.follow.dto.FollowListResponse;
import com.stylemycloset.follow.dto.FollowResult;
import com.stylemycloset.follow.dto.FollowSummaryResult;
import com.stylemycloset.follow.dto.request.FollowCreateRequest;
import com.stylemycloset.follow.dto.request.SearchFollowersCondition;
import com.stylemycloset.follow.dto.request.SearchFollowingsCondition;
import com.stylemycloset.follow.entity.Follow;
import com.stylemycloset.follow.exception.FollowNotFoundException;
import com.stylemycloset.follow.mapper.FollowMapper;
import com.stylemycloset.follow.repository.FollowRepository;
import com.stylemycloset.follow.service.FollowService;
import com.stylemycloset.follow.service.UserRepository;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.stylemycloset.user.entity.User;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

  private final FollowRepository followRepository;
  private final UserRepository userRepository;
  private final FollowMapper followMapper;

  @Transactional
  @Override
  public FollowResult startFollowing(FollowCreateRequest followCreateRequest) {
    User follower = userRepository.findById(followCreateRequest.followerId())
        .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));
    User followee = userRepository.findById(followCreateRequest.followeeId())
        .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));
    validateDuplicateFollow(follower, followee);

    Follow savedFollowShip = followRepository.save(new Follow(follower, followee, Instant.now()));
    // 자기자신 팔로우 못하게 해야함
    return followMapper.toResult(savedFollowShip);
  }

  @Transactional(readOnly = true)
  @Override
  public FollowSummaryResult summaryFollowInfo(Long targetUserId, Long logInUserId) {
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

  private void validateDuplicateFollow(User follower, User followee) {
    // 후에 softDelete 한거 조정필요
    if (followRepository.existsByFollowerIdAndFolloweeId(follower.getId(), followee.getId())) {
      throw new IllegalArgumentException("중복되 팔로우 관계가 있습니다.");
    }
  }

}

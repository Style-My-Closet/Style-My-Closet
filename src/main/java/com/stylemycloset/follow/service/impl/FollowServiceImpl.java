package com.stylemycloset.follow.service.impl;

import com.stylemycloset.follow.dto.FollowListResponse;
import com.stylemycloset.follow.dto.FollowResult;
import com.stylemycloset.follow.dto.FollowSummaryResult;
import com.stylemycloset.follow.dto.request.FollowCreateRequest;
import com.stylemycloset.follow.dto.request.SearchFollowersCondition;
import com.stylemycloset.follow.dto.request.SearchFollowingsCondition;
import com.stylemycloset.follow.entity.Follow;
import com.stylemycloset.follow.exception.FollowAlreadyExist;
import com.stylemycloset.follow.exception.FollowSelfForbiddenException;
import com.stylemycloset.follow.mapper.FollowMapper;
import com.stylemycloset.follow.repository.FollowRepository;
import com.stylemycloset.follow.service.FollowService;
import com.stylemycloset.follow.service.UserRepository;
import com.stylemycloset.user.entity.User;

import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
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
    validateSelfFollow(followCreateRequest.followeeId(), followCreateRequest.followerId());
    User follower = getUser(followCreateRequest.followerId());
    User followee = getUser(followCreateRequest.followeeId());
    Optional<Follow> optionalFollow = followRepository.findDeletedByFolloweeIdAndFollowerId(
        followCreateRequest.followeeId(),
        followCreateRequest.followerId()
    );

    Follow follow = restoreOrCreateFollow(followee, follower, optionalFollow);
    Follow savedFollow = followRepository.save(follow);
    return followMapper.toResult(savedFollow);
  }

  private User getUser(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다. id=" + userId));
  }

  private Follow restoreOrCreateFollow(
      User followee,
      User follower,
      Optional<Follow> optionalFollow
  ) {
    if (optionalFollow.isEmpty()) {
      return new Follow(followee, follower);
    }
    Follow follow = optionalFollow.get();
    if (follow.isSoftDeleted()) {
      follow.restore();
      return follow;
    }
    throw new FollowAlreadyExist(Map.of());
  }

  @Transactional(readOnly = true)
  @Override
  public FollowSummaryResult summaryFollow(Long followee, Long follower) {
    long followersNumber = followRepository.countActiveFollowers(follower);
    long followingsNumber = followRepository.countActiveFollowings(follower);
    Follow userToTargetFollow = followRepository.findActiveByFolloweeIdAndFollowerId(
            followee, follower)
        .orElse(null);
    boolean isFollowingMe = followRepository.existsActiveByFolloweeIdAndFollowerId(
        followee, follower);

    return FollowSummaryResult.of(
        followee,
        followersNumber,
        followingsNumber,
        userToTargetFollow,
        isFollowingMe
    );
  }

  @Transactional(readOnly = true)
  @Override
  public FollowListResponse<FollowResult> getFollowings(
      SearchFollowingsCondition followingsCondition
  ) {
    Slice<Follow> followings = followRepository.findFollowingsByFollowerId(
        followingsCondition.followerId(),
        followingsCondition.cursor(),
        followingsCondition.idAfter(),
        followingsCondition.limit(),
        followingsCondition.nameLike(),
        followingsCondition.sortBy(),
        followingsCondition.sortDirection()
    );

    return followMapper.toFollowResponse(followings);
  }

  @Transactional(readOnly = true)
  @Override
  public FollowListResponse<FollowResult> getFollowers(
      SearchFollowersCondition followersCondition
  ) {

    // d.followee = :followeeId 인 팔로워를 봅니다.
    return null;
  }

  @Transactional
  @Override
  public void softDelete(Long followId) {
  }

  @Transactional
  @Override
  public void hardDelete(Long followId) {
  }

  private void validateSelfFollow(Long followeeId, Long followerId) {
    if (followeeId.equals(followerId)) {
      throw new FollowSelfForbiddenException(Map.of());
    }
  }

}

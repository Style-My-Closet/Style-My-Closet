package com.stylemycloset.follow.service.impl;

import com.stylemycloset.follow.dto.FollowListResponse;
import com.stylemycloset.follow.dto.FollowResult;
import com.stylemycloset.follow.dto.FollowSummaryResult;
import com.stylemycloset.follow.dto.request.FollowCreateRequest;
import com.stylemycloset.follow.dto.request.SearchFollowersCondition;
import com.stylemycloset.follow.dto.request.SearchFollowingsCondition;
import com.stylemycloset.follow.entity.Follow;
import com.stylemycloset.follow.exception.ActiveFollowNotFoundException;
import com.stylemycloset.follow.exception.FollowAlreadyExist;
import com.stylemycloset.follow.exception.FollowNotFoundException;
import com.stylemycloset.follow.exception.FollowSelfForbiddenException;
import com.stylemycloset.follow.mapper.FollowMapper;
import com.stylemycloset.follow.repository.FollowRepository;
import com.stylemycloset.follow.service.FollowService;
import com.stylemycloset.follow.service.UserRepository;
import com.stylemycloset.user.entity.User;
import java.util.Map;
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
    validateFollowAlreadyExist(followCreateRequest.followeeId(), followCreateRequest.followerId());
    User follower = getUser(followCreateRequest.followerId());
    User followee = getUser(followCreateRequest.followeeId());

    Follow follow = followRepository.findSoftDeletedByFolloweeIdAndFollowerId(
        followCreateRequest.followeeId(),
        followCreateRequest.followerId()
    ).map(deletedFollow -> {
      deletedFollow.restore();
      return deletedFollow;
    }).orElseGet(() -> new Follow(followee, follower));
    Follow savedFollow = followRepository.save(follow);

    return followMapper.toResult(savedFollow);
  }

  @Transactional(readOnly = true)
  @Override
  public FollowSummaryResult summaryFollow(Long userId, Long logInUserId) {
    long followersNumber = followRepository.countActiveFollowers(userId);
    long followingsNumber = followRepository.countActiveFollowings(userId);
    Follow logInUserFollowTargetUser = followRepository.findActiveByFolloweeIdAndFollowerId(
        userId, logInUserId
    ).orElseGet(() -> null);
    boolean isFollowingMe = followRepository.existsActiveByFolloweeIdAndFollowerId(
        logInUserId, userId
    );

    return FollowSummaryResult.of(
        userId,
        followersNumber,
        followingsNumber,
        logInUserFollowTargetUser,
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
    Slice<Follow> followings = followRepository.findFollowersByFolloweeId(
        followersCondition.followeeId(),
        followersCondition.cursor(),
        followersCondition.idAfter(),
        followersCondition.limit(),
        followersCondition.nameLike(),
        followersCondition.sortBy(),
        followersCondition.sortDirection()
    );

    return followMapper.toFollowResponse(followings);
  }

  @Transactional
  @Override
  public void softDelete(Long followId) {
    Follow follow = followRepository.findActiveById(followId)
        .orElseThrow(() -> new ActiveFollowNotFoundException(Map.of()));

    follow.softDelete();
    followRepository.save(follow);
  }

  @Transactional
  @Override
  public void hardDelete(Long followId) {
    validateFollowExist(followId);
    followRepository.deleteById(followId);
  }

  private void validateFollowExist(Long followId) {
    if (followRepository.existsById(followId)) {
      return;
    }
    throw new FollowNotFoundException(Map.of());
  }

  private void validateSelfFollow(Long followeeId, Long followerId) {
    if (followeeId.equals(followerId)) {
      throw new FollowSelfForbiddenException(Map.of());
    }
  }

  private void validateFollowAlreadyExist(Long followeeId, Long followerId) {
    if (followRepository.existsActiveByFolloweeIdAndFollowerId(followeeId, followerId)) {
      throw new FollowAlreadyExist(Map.of());
    }
  }

  private User getUser(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다. id=" + userId));
  }

}

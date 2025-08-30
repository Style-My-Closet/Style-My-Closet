package com.stylemycloset.follow.service.impl;

import com.stylemycloset.follow.dto.FollowListResponse;
import com.stylemycloset.follow.dto.FollowResult;
import com.stylemycloset.follow.dto.FollowSummaryResult;
import com.stylemycloset.follow.dto.request.FollowCreateRequest;
import com.stylemycloset.follow.dto.request.SearchFollowersCondition;
import com.stylemycloset.follow.dto.request.SearchFollowingsCondition;
import com.stylemycloset.follow.entity.Follow;
import com.stylemycloset.follow.exception.ActiveFollowNotFoundException;
import com.stylemycloset.follow.exception.FollowAlreadyExistException;
import com.stylemycloset.follow.exception.FollowNotFoundException;
import com.stylemycloset.follow.exception.FollowSelfForbiddenException;
import com.stylemycloset.follow.mapper.FollowMapper;
import com.stylemycloset.follow.repository.FollowRepository;
import com.stylemycloset.follow.service.FollowService;
import com.stylemycloset.notification.event.domain.FollowEvent;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.exception.UserNotFoundException;
import com.stylemycloset.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

  private final FollowRepository followRepository;
  private final UserRepository userRepository;
  private final FollowMapper followMapper;
  private final ApplicationEventPublisher publisher;

  @Transactional
  @Override
  public FollowResult startFollowing(FollowCreateRequest followCreateRequest) {
    validateSelfFollow(followCreateRequest.followeeId(), followCreateRequest.followerId());
    validateFollowAlreadyExist(followCreateRequest.followeeId(), followCreateRequest.followerId());
    User follower = getUser(followCreateRequest.followerId());
    User followee = getUser(followCreateRequest.followeeId());
    Follow savedFollow = followRepository.save(new Follow(followee, follower));

    publisher.publishEvent(new FollowEvent(followee.getId(), follower.getName()));

    return followMapper.toResult(savedFollow);
  }

  @Transactional(readOnly = true)
  @Override
  public FollowSummaryResult getFollowSummary(Long userId, Long viewerId) {
    long followersNumber = followRepository.countActiveFollowers(userId);
    long followingsNumber = followRepository.countActiveFollowings(userId);
    Follow logInUserFollowTargetUser = followRepository.findActiveByFolloweeIdAndFollowerId(
        userId,
        viewerId
    ).orElse(null);
    boolean isFollowingMe = followRepository.existsActiveByFolloweeIdAndFollowerId(
        viewerId,
        userId
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
    Follow follow = followRepository.findById(followId)
        .orElseThrow(ActiveFollowNotFoundException::new);
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
    throw new FollowNotFoundException();
  }

  private void validateSelfFollow(Long followeeId, Long followerId) {
    if (followeeId.equals(followerId)) {
      throw new FollowSelfForbiddenException();
    }
  }

  private void validateFollowAlreadyExist(Long followeeId, Long followerId) {
    if (followRepository.existsActiveByFolloweeIdAndFollowerId(followeeId, followerId)) {
      throw new FollowAlreadyExistException();
    }
  }

  private User getUser(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);
  }

}

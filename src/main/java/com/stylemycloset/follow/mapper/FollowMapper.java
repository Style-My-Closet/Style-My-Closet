package com.stylemycloset.follow.mapper;

import com.stylemycloset.binarycontent.mapper.BinaryContentMapper;
import com.stylemycloset.common.repository.CustomSliceImpl;
import com.stylemycloset.common.repository.NextCursorInfo;
import com.stylemycloset.follow.dto.FollowListResponse;
import com.stylemycloset.follow.dto.FollowResult;
import com.stylemycloset.follow.dto.FollowUserInfo;
import com.stylemycloset.follow.entity.Follow;
import com.stylemycloset.user.entity.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FollowMapper {

  private final BinaryContentMapper binaryContentMapper;

  public FollowResult toResult(Follow follow) {
    User followee = follow.getFollowee();
    User follower = follow.getFollower();

    return FollowResult.from(
        follow,
        FollowUserInfo.of(
            followee,
            binaryContentMapper.extractUrl(followee.getProfileImage())
        ),
        FollowUserInfo.of(
            follower,
            binaryContentMapper.extractUrl(follower.getProfileImage())
        )
    );
  }

  public FollowListResponse<FollowResult> toFollowResponse(Slice<Follow> follows) {
    List<FollowResult> followResults = getFollowResults(follows);
    Order order = CustomSliceImpl.getOrder(follows);

    return FollowListResponse.of(
        followResults,
        NextCursorInfo.followCursor(follows, order.getProperty()),
        follows.hasNext(),
        null,
        order.getProperty(),
        order.getDirection().toString()
    );
  }

  private List<FollowResult> getFollowResults(Slice<Follow> follows) {
    return follows.getContent()
        .stream()
        .map(this::toResult)
        .toList();
  }

}

package com.stylemycloset.follow.mapper;

import com.stylemycloset.binarycontent.storage.BinaryContentStorage;
import com.stylemycloset.follow.dto.FollowListResponse;
import com.stylemycloset.follow.dto.FollowListResponse.NextCursorInfo;
import com.stylemycloset.follow.dto.FollowResult;
import com.stylemycloset.follow.dto.FollowUserInfo;
import com.stylemycloset.follow.entity.Follow;
import com.stylemycloset.follow.repository.querydsl.cursor.CursorStrategy;
import com.stylemycloset.follow.repository.querydsl.cursor.FollowCursorField;
import com.stylemycloset.user.entity.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FollowMapper {

  private final BinaryContentStorage binaryContentStorage;

  public FollowResult toResult(Follow follow) {
    User followee = follow.getFollowee();
    User follower = follow.getFollower();

    return FollowResult.from(
        follow,
        FollowUserInfo.of(followee, getProfileImageURL(followee)),
        FollowUserInfo.of(follower, getProfileImageURL(follower))
    );
  }

  private String getProfileImageURL(User user) {
    if (user.getProfileImage() == null) {
      return null;
    }
    return binaryContentStorage.getUrl(user.getProfileImage().getId())
        .toString();
  }

  public FollowListResponse<FollowResult> toFollowResponse(Slice<Follow> follows) {
    List<FollowResult> followResults = getFollowResults(follows);
    Order order = getOrder(follows);
    String sortBy = order.getProperty();

    return FollowListResponse.from(
        followResults,
        extractNextCursorInfo(follows, sortBy),
        follows.hasNext(),
        null,
        order.getProperty(),
        order.getDirection().toString()
    );
  }

  private Order getOrder(Slice<Follow> follows) {
    return follows.getPageable()
        .getSort()
        .iterator()
        .next();
  }

  private List<FollowResult> getFollowResults(Slice<Follow> follows) {
    return follows.getContent()
        .stream()
        .map(this::toResult)
        .toList();
  }

  private NextCursorInfo extractNextCursorInfo(Slice<Follow> follows, String sortBy) {
    if (!follows.hasNext() || follows.getContent().isEmpty()) {
      return new NextCursorInfo(null, null);
    }

    Follow lastFollow = follows.getContent().get(follows.getContent().size() - 1);
    CursorStrategy<?> cursorStrategy = FollowCursorField.resolveStrategy(sortBy);
    String cursor = cursorStrategy.extract(lastFollow).toString();
    String idAfter = lastFollow.getId().toString();

    return new NextCursorInfo(cursor, idAfter);
  }

}

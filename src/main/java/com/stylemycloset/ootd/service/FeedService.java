package com.stylemycloset.ootd.service;

import com.stylemycloset.ootd.dto.CommentCreateRequest;
import com.stylemycloset.ootd.dto.CommentCursorResponse;
import com.stylemycloset.ootd.dto.CommentDto;
import com.stylemycloset.ootd.dto.CommentSearchRequest;
import com.stylemycloset.ootd.dto.FeedCreateRequest;
import com.stylemycloset.ootd.dto.FeedDto;
import com.stylemycloset.ootd.dto.FeedDtoCursorResponse;
import com.stylemycloset.ootd.dto.FeedSearchRequest;
import com.stylemycloset.ootd.dto.FeedUpdateRequest;
import com.stylemycloset.weather.entity.Weather.SkyStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;

public interface FeedService {

  FeedDto createFeed(FeedCreateRequest request);

  FeedDtoCursorResponse getFeeds(FeedSearchRequest request, @Nullable Long currentUserId);

  FeedDto toggleLike(Long userId, Long feedId);

  FeedDto updateFeed(Long userId, Long feedId, FeedUpdateRequest request);

  void deleteFeed(Long userId, Long feedId);

  CommentCursorResponse getComments(Long feedId, CommentSearchRequest request);

  CommentDto createComment(CommentCreateRequest request, Long currentUserId);
}

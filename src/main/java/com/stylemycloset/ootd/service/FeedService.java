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

  /**
   * 피드 목록을 조회합니다.
   * 
   * @param request 피드 검색 조건
   * @param currentUserId 현재 인증된 사용자 ID. null이면 비인증 사용자로 간주한다.
   *                     - likedByMe: 항상 false로 매핑
   *                     - 사용자 기반 배치 쿼리(findFeedIdsByUserAndFeedIds): 호출하지 않음
   * @return 피드 목록 응답
   */
  FeedDtoCursorResponse getFeeds(FeedSearchRequest request, @Nullable Long currentUserId);

  FeedDto toggleLike(Long userId, Long feedId);

  FeedDto updateFeed(Long userId, Long feedId, FeedUpdateRequest request);

  void deleteFeed(Long userId, Long feedId);

  CommentCursorResponse getComments(Long feedId, CommentSearchRequest request);

  CommentDto createComment(CommentCreateRequest request, Long currentUserId);
}

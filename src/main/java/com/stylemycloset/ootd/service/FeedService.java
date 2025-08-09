package com.stylemycloset.ootd.service;

import com.stylemycloset.ootd.dto.FeedCreateRequest;
import com.stylemycloset.ootd.dto.FeedDto;
import com.stylemycloset.ootd.dto.FeedDtoCursorResponse;
import com.stylemycloset.ootd.dto.FeedSearchRequest;

public interface FeedService {

  FeedDto createFeed(FeedCreateRequest request);

  FeedDtoCursorResponse getFeeds(FeedSearchRequest request);

  FeedDto likeFeed(Long userId, Long feedId); // 좋아요

  void unlikeFeed(Long userId, Long feedId); // 좋아요 취소
}

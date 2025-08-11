package com.stylemycloset.ootd.service;

import com.stylemycloset.ootd.dto.FeedCreateRequest;
import com.stylemycloset.ootd.dto.FeedDto;
import com.stylemycloset.ootd.dto.FeedDtoCursorResponse;
import com.stylemycloset.weather.entity.Weather.SkyStatus;
import org.springframework.data.domain.Pageable;

public interface FeedService {

  FeedDto createFeed(FeedCreateRequest request);

  FeedDtoCursorResponse getFeeds(Long cursorId, String keywordLike, SkyStatus skyStatus,
      Long authorId, Pageable pageable);
}

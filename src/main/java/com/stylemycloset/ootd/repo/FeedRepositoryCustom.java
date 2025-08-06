package com.stylemycloset.ootd.repo;

import com.stylemycloset.ootd.dto.FeedDtoCursorResponse;
import com.stylemycloset.weather.entity.Weather.SkyStatus;
import org.springframework.data.domain.Pageable;

public interface FeedRepositoryCustom {
  FeedDtoCursorResponse findByConditions(Long cursorId, String keywordLike, SkyStatus skyStatus, Long authorId, Pageable pageable);
}

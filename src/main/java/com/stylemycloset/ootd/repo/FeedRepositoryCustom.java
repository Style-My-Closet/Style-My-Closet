package com.stylemycloset.ootd.repo;

import com.stylemycloset.ootd.dto.FeedSearchRequest;
import com.stylemycloset.ootd.entity.Feed;
import com.stylemycloset.weather.entity.Weather.SkyStatus;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface FeedRepositoryCustom {

  List<Feed> findByConditions(FeedSearchRequest request, Pageable pageable);
}

package com.stylemycloset.ootd.repository;

import com.stylemycloset.ootd.dto.FeedSearchRequest;
import com.stylemycloset.ootd.entity.Feed;
import java.util.List;

public interface FeedRepositoryCustom {

  List<Feed> findByConditions(FeedSearchRequest request);
}

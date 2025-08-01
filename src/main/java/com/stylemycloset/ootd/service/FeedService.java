package com.stylemycloset.ootd.service;

import com.stylemycloset.ootd.dto.FeedCreateRequest;
import com.stylemycloset.ootd.dto.FeedDto;

public interface FeedService {

  FeedDto createFeed(FeedCreateRequest request);
}

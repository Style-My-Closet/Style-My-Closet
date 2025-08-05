package com.stylemycloset.ootd.service;

import com.stylemycloset.ootd.dto.FeedCreateRequest;
import com.stylemycloset.ootd.dto.FeedDto;

public interface FeedService {

  /**
 * 피드 생성 요청을 받아 새로운 피드를 생성하고, 생성된 피드 정보를 반환합니다.
 *
 * @param request 피드 생성에 필요한 데이터가 담긴 요청 객체
 * @return 생성된 피드의 상세 정보를 담은 DTO
 */
FeedDto createFeed(FeedCreateRequest request);
}

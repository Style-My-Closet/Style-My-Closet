package com.stylemycloset.ootd.dto;

import com.stylemycloset.ootd.tempEnum.PrecipitationType;
import com.stylemycloset.weather.entity.Weather.SkyStatus;
import lombok.Builder;

@Builder
public record FeedSearchRequest(
    // 페이징 관련
    String cursor,
    Long idAfter,
    Integer limit,
    String sortBy,
    String sortDirection,

    // 필터링(검색) 관련
    String keywordLike,
    SkyStatus skyStatusEqual,
    PrecipitationType precipitationTypeEqual,
    Long authorIdEqual
) {

}
package com.stylemycloset.ootd.dto;

import com.stylemycloset.ootd.tempEnum.PrecipitationType;
import com.stylemycloset.weather.entity.Weather.SkyStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record FeedSearchRequest(
    // 페이징 관련
    String cursor,
    Long idAfter,
    @NotNull(message = "limit 값은 필수입니다.")
    Integer limit,

    @NotNull(message = "sortBy 값은 필수입니다.")
    String sortBy,

    @NotNull(message = "sortDirection 값은 필수입니다.")
    String sortDirection,

    // 필터링(검색) 관련
    String keywordLike,
    SkyStatus skyStatusEqual,
    PrecipitationType precipitationTypeEqual,
    Long authorIdEqual
) {

}
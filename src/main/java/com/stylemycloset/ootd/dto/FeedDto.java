package com.stylemycloset.ootd.dto;

import java.time.Instant;
import java.util.List;

public record FeedDto(
    Long id,
    Instant createdAt,
    Instant updatedAt,
    AuthorDto author,
    WeatherSummaryDto weather,
    List<OotdItemDto> ootds,
    String content,
    Long likeCount,
    Integer commentCount,
    Boolean likedByMe
) {

}

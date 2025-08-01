package com.stylemycloset.ootd.dto;

//import 나중에 진짜 cloth dto user dto 완성되면 추가
import com.stylemycloset.weather.dto.WeatherSummaryDto;
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

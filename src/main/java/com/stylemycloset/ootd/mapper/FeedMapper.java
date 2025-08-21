package com.stylemycloset.ootd.mapper;

import com.stylemycloset.ootd.dto.AuthorDto;
import com.stylemycloset.ootd.dto.FeedDto;
import com.stylemycloset.weather.dto.WeatherSummaryDto;
import com.stylemycloset.ootd.entity.Feed;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.weather.entity.Weather;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Feed 엔티티를 FeedDto로 변환하는 매퍼
 * 
 * 책임:
 * - Feed 엔티티의 전체 매핑 로직 처리
 * - Author, Weather, OotdItem 매핑 조합
 * - 좋아요 정보 및 댓글 수 매핑
 */
@Component
public class FeedMapper {

    private final OotdItemMapper ootdItemMapper;

    public FeedMapper(OotdItemMapper ootdItemMapper) {
        this.ootdItemMapper = ootdItemMapper;
    }

    /**
     * Feed 엔티티를 FeedDto로 변환
     * 
     * @param feed 변환할 Feed 엔티티
     * @param currentUser 현재 인증된 사용자 (좋아요 상태 확인용)
     * @param likeCount 좋아요 수
     * @param commentCount 댓글 수
     * @param likedByMe 현재 사용자의 좋아요 여부
     * @return FeedDto
     */
    public FeedDto toDto(Feed feed, User currentUser, Long likeCount, Integer commentCount, Boolean likedByMe) {
        if (feed == null) {
            return null;
        }

        AuthorDto authorDto = toAuthorDto(feed.getAuthor());
        WeatherSummaryDto weatherDto = toWeatherSummaryDto(feed.getWeather());
        
        // OotdItemDto 리스트 생성
        List<com.stylemycloset.ootd.dto.OotdItemDto> ootdItemDtos = feed.getFeedClothes().stream()
            .map(feedClothes -> ootdItemMapper.toDto(feedClothes.getClothes()))
            .collect(Collectors.toList());

        return new FeedDto(
            feed.getId(),
            feed.getCreatedAt(),
            feed.getUpdatedAt(),
            authorDto,
            weatherDto,
            ootdItemDtos,
            feed.getContent(),
            likeCount,
            commentCount,
            likedByMe
        );
    }

    /**
     * Feed 리스트를 FeedDto 리스트로 변환
     * 
     * @param feeds 변환할 Feed 엔티티 리스트
     * @param currentUser 현재 인증된 사용자
     * @param likeCountMap 좋아요 수 맵
     * @param commentCountMap 댓글 수 맵
     * @param likedByMeMap 좋아요 상태 맵
     * @return FeedDto 리스트
     */
    public List<FeedDto> toDtoList(List<Feed> feeds, User currentUser, 
                                  Map<Long, Long> likeCountMap,
                                  Map<Long, Integer> commentCountMap,
                                  Map<Long, Boolean> likedByMeMap) {
        if (feeds == null) {
            return List.of();
        }

        return feeds.stream()
            .map(feed -> {
                Long likeCount = likeCountMap.getOrDefault(feed.getId(), 0L);
                Integer commentCount = commentCountMap.getOrDefault(feed.getId(), 0);
                Boolean likedByMe = likedByMeMap.getOrDefault(feed.getId(), false);
                
                return toDto(feed, currentUser, likeCount, commentCount, likedByMe);
            })
            .collect(Collectors.toList());
    }

    /**
     * User 엔티티를 AuthorDto로 변환
     * 
     * @param user 변환할 User 엔티티
     * @return AuthorDto
     */
    public AuthorDto toAuthorDto(User user) {
        if (user == null) {
            return null;
        }
        return new AuthorDto(user.getId(), user.getName(), null);
    }

    /**
     * Weather 엔티티를 WeatherSummaryDto로 변환
     * 
     * @param weather 변환할 Weather 엔티티
     * @return WeatherSummaryDto
     */
    private WeatherSummaryDto toWeatherSummaryDto(Weather weather) {
        if (weather == null) {
            return null;
        }
        
        // PrecipitationDto 생성
        com.stylemycloset.weather.dto.PrecipitationDto precipitationDto = 
            new com.stylemycloset.weather.dto.PrecipitationDto(
                Weather.AlertType.valueOf(weather.getPrecipitation().getAlertType().name().toUpperCase()),
                weather.getPrecipitation().getAmount(),
                weather.getPrecipitation().getProbability()
            );
        
        // TemperatureDto 생성
        com.stylemycloset.weather.dto.TemperatureDto temperatureDto = 
            new com.stylemycloset.weather.dto.TemperatureDto(
                weather.getTemperature().getCurrent(),
                weather.getTemperature().getComparedToDayBefore(),
                weather.getTemperature().getMin(),
                weather.getTemperature().getMax()
            );

        return new WeatherSummaryDto(
            weather.getId(), 
            weather.getSkyStatus(), 
            precipitationDto,
            temperatureDto
        );
    }
}

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

@Component
public class FeedMapper {

    private final OotdItemMapper ootdItemMapper;

    public FeedMapper(OotdItemMapper ootdItemMapper) {
        this.ootdItemMapper = ootdItemMapper;
    }

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
                likedByMe);
    }

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

    public AuthorDto toAuthorDto(User user) {
        if (user == null) {
            return null;
        }
        return new AuthorDto(user.getId(), user.getName(), null);
    }

    private WeatherSummaryDto toWeatherSummaryDto(Weather weather) {
        if (weather == null) {
            return null;
        }

        // PrecipitationDto 생성
        com.stylemycloset.weather.dto.PrecipitationDto precipitationDto = new com.stylemycloset.weather.dto.PrecipitationDto(
                Weather.AlertType.valueOf(weather.getPrecipitation().getAlertType().name().toUpperCase()),
                weather.getPrecipitation().getAmount(),
                weather.getPrecipitation().getProbability());

        // TemperatureDto 생성
        com.stylemycloset.weather.dto.TemperatureDto temperatureDto = new com.stylemycloset.weather.dto.TemperatureDto(
                weather.getTemperature().getCurrent(),
                weather.getTemperature().getComparedToDayBefore(),
                weather.getTemperature().getMin(),
                weather.getTemperature().getMax());

        return new WeatherSummaryDto(
                weather.getId(),
                weather.getSkyStatus(),
                precipitationDto,
                temperatureDto);
    }
}

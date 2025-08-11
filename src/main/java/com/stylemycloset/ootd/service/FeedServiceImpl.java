package com.stylemycloset.ootd.service;

import com.stylemycloset.cloth.repository.ClothRepository;
import com.stylemycloset.ootd.dto.AuthorDto;
import com.stylemycloset.cloth.entity.Cloth;
import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import com.stylemycloset.ootd.dto.ClothesAttributeWithDefDto;
import com.stylemycloset.ootd.dto.FeedCreateRequest;
import com.stylemycloset.ootd.dto.FeedDto;
import com.stylemycloset.ootd.dto.FeedDtoCursorResponse;
import com.stylemycloset.ootd.dto.OotdItemDto;
import com.stylemycloset.ootd.entity.Feed;
import com.stylemycloset.ootd.entity.FeedClothes;
import com.stylemycloset.ootd.repo.FeedClothesRepository;
import com.stylemycloset.ootd.repo.FeedRepository;
import com.stylemycloset.ootd.tempEnum.ClothesType;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import com.stylemycloset.weather.dto.PrecipitationDto;
import com.stylemycloset.weather.dto.TemperatureDto;
import com.stylemycloset.weather.dto.WeatherSummaryDto;
import com.stylemycloset.weather.entity.Weather;
import com.stylemycloset.weather.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedServiceImpl implements FeedService {

  private final FeedRepository feedRepository;
  private final FeedClothesRepository feedClothesRepository;
  private final UserRepository userRepository;
  private final ClothRepository clothRepository;
  private final WeatherRepository weatherRepository;

  @Override
  public FeedDto createFeed(FeedCreateRequest request) {
    User author = userRepository.findById(request.authorId())
        .orElseThrow(() -> new StyleMyClosetException(ErrorCode.USER_NOT_FOUND,
            Map.of("userId", request.authorId())));

    Weather weather = findWeatherOrNull(request.weatherId());

    List<Cloth> clothesList = clothRepository.findAllById(request.clothesIds());
    if (clothesList.size() != request.clothesIds().size()) {
      throw new StyleMyClosetException(ErrorCode.CLOTHES_NOT_FOUND,
          Map.of("requestedIds", request.clothesIds()));
    }

    Feed newFeed = Feed.createFeed(author, weather, request.content());

    List<FeedClothes> feedClothesList = clothesList.stream()
        .map(cloth -> FeedClothes.createFeedClothes(newFeed, cloth))
        .collect(Collectors.toList());

    feedRepository.save(newFeed);
    feedClothesRepository.saveAll(feedClothesList);

    return mapToFeedResponse(newFeed);
  }

  @Override
  public FeedDtoCursorResponse getFeeds(Long cursorId, String keywordLike, Weather.SkyStatus skyStatus, Long authorId, Pageable pageable) {
    List<Feed> feeds = feedRepository.findByConditions(cursorId, keywordLike, skyStatus, authorId, pageable);

    boolean hasNext = feeds.size() > pageable.getPageSize();
    if (hasNext) {
      feeds.remove(pageable.getPageSize());
    }

    String nextCursor = null;
    if (hasNext && !feeds.isEmpty()) {
      nextCursor = feeds.get(feeds.size() - 1).getId().toString();
    }

    List<FeedDto> feedDtos = feeds.stream()
        .map(this::mapToFeedResponse)
        .collect(Collectors.toList());

    return new FeedDtoCursorResponse(
        feedDtos, nextCursor, nextCursor, hasNext, 0L, "createdAt", "DESCENDING");
  }

  private Weather findWeatherOrNull(Long weatherId) {
    if (weatherId == null) {
      return null;
    }
    return weatherRepository.findById(weatherId)
        .orElseThrow(() -> new StyleMyClosetException(ErrorCode.WEATHER_NOT_FOUND,
            Map.of("weatherId", weatherId)));
  }

  private FeedDto mapToFeedResponse(Feed feed) {
    List<Cloth> clothesList = feed.getFeedClothes().stream()
        .map(FeedClothes::getClothes)
        .collect(Collectors.toList());

    AuthorDto authorDto = toAuthorDto(feed.getAuthor());
    WeatherSummaryDto weatherDto = toWeatherSummaryDto(feed.getWeather());
    List<OotdItemDto> ootdItemDtos = toOotdItemDtoList(clothesList);

    return new FeedDto(
        feed.getId(),
        feed.getCreatedAt(),
        feed.getUpdatedAt(),
        authorDto,
        weatherDto,
        ootdItemDtos,
        feed.getContent(),
        0L, // TODO: 좋아요 수 계산 로직 추가 필요
        0,  // TODO: 댓글 수 계산 로직 추가 필요
        false // TODO: 내가 좋아요 눌렀는지 확인하는 로직 추가 필요
    );
  }

  private AuthorDto toAuthorDto(User author) {
    if (author == null) return null;
    return new AuthorDto(author.getId(), author.getName(), null);
  }

  private WeatherSummaryDto toWeatherSummaryDto(Weather weather) {
    if (weather == null) return null;
    PrecipitationDto precipitationDto = new PrecipitationDto(
        Weather.AlertType.valueOf(weather.getPrecipitation().getType().toUpperCase()),
        weather.getPrecipitation().getAmount(),
        weather.getPrecipitation().getProbability()
    );
    TemperatureDto temperatureDto = new TemperatureDto(
        weather.getTemperature().getCurrent(),
        weather.getTemperature().getComparedToDayBefore(),
        weather.getTemperature().getMin(),
        weather.getTemperature().getMax()
    );
    return new WeatherSummaryDto(weather.getId(), weather.getSkyStatus(), precipitationDto, temperatureDto);
  }

  private List<OotdItemDto> toOotdItemDtoList(List<Cloth> clothesList) {
    return clothesList.stream().map(this::toOotdItemDto).collect(Collectors.toList());
  }

  private OotdItemDto toOotdItemDto(Cloth cloth) {
    List<ClothesAttributeWithDefDto> attributes = new ArrayList<>();
    // TODO: cloth의 속성 정보를 attributes 리스트에 채우는 로직 구현
    return new OotdItemDto(cloth.getId(), cloth.getName(), null, ClothesType.valueOf(cloth.getCategory().getName().name()), attributes);
  }
}
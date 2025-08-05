package com.stylemycloset.ootd.service;

import com.stylemycloset.ootd.dto.ClothesAttributeWithDefDto; // 나중에 import 변경 예정
import com.stylemycloset.cloth.entity.Cloth;
import com.stylemycloset.cloth.repository.ClothRepository; // 나중에 import 변경 예정
import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import com.stylemycloset.ootd.dto.FeedCreateRequest;
import com.stylemycloset.ootd.dto.FeedDto;
import com.stylemycloset.ootd.dto.OotdItemDto; // 나중에 import 변경 예정
import com.stylemycloset.ootd.entity.Feed;
import com.stylemycloset.ootd.entity.FeedClothes;
import com.stylemycloset.ootd.repo.FeedClothesRepository;
import com.stylemycloset.ootd.repo.FeedRepository;
import com.stylemycloset.ootd.repo.UserRepository; // 나중에 import 변경 예정
import com.stylemycloset.ootd.tempEnum.ClothesType; // 나중에 import 변경 예정
import com.stylemycloset.ootd.dto.AuthorDto; // 나중에 import 변경 예정
import com.stylemycloset.user.entity.User;
import com.stylemycloset.weather.dto.PrecipitationDto;
import com.stylemycloset.weather.dto.TemperatureDto;
import com.stylemycloset.weather.dto.WeatherSummaryDto;
import com.stylemycloset.weather.entity.Weather;
import com.stylemycloset.weather.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
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
    feedRepository.save(newFeed);

    List<FeedClothes> feedClothesList = clothesList.stream()
        .map(cloth -> FeedClothes.createFeedClothes(newFeed, cloth))
        .collect(Collectors.toList());
    feedClothesRepository.saveAll(feedClothesList);

    return mapToFeedResponse(newFeed, clothesList);
  }

  private Weather findWeatherOrNull(Long weatherId) {
    if (weatherId == null) {
      return null;
    }
    return weatherRepository.findById(weatherId)
        .orElseThrow(() -> new StyleMyClosetException(ErrorCode.WEATHER_NOT_FOUND,
            Map.of("weatherId", weatherId)));
  }

  private FeedDto mapToFeedResponse(Feed feed, List<Cloth> clothesList) {
    AuthorDto authorDto = new AuthorDto(
        feed.getAuthor().getId(),
        feed.getAuthor().getName(),
        null // TODO: User 엔티티에 profileImageUrl 필드가 추가되면 여기에 연결
    );

//    WeatherSummaryDto weatherDto = null;
//    if (feed.getWeather() != null) {
//      Weather weather = feed.getWeather();
//      PrecipitationDto precipitationDto = new PrecipitationDto(
//          Weather.AlertType.valueOf(weather.getPrecipitation().getType().toUpperCase()),
//          weather.getPrecipitation().getAmount(),
//          weather.getPrecipitation().getProbability()
//      );
//      TemperatureDto temperatureDto = new TemperatureDto(
//          weather.getTemperature().getCurrent(),
//          weather.getTemperature().getComparedToDayBefore(),
//          weather.getTemperature().getMin(),
//          weather.getTemperature().getMax()
//      );
//      weatherDto = new WeatherSummaryDto(
//          weather.getId(),
//          weather.getSkyStatus(),
//          precipitationDto,
//          temperatureDto
//      );
//    }

    List<OotdItemDto> ootdItemDtos = clothesList.stream()
        .map(cloth -> {
          List<ClothesAttributeWithDefDto> attributes = new ArrayList<>();
          // TODO: cloth의 속성 정보를 attributes 리스트에 채우는 로직 구현

          return new OotdItemDto(
              cloth.getId(),
              cloth.getName(),
              null, // TODO: Cloth 엔티티에 imageUrl 필드가 있다면 추가 (binaryContent 사용)
              ClothesType.valueOf(cloth.getCategory().getName().name()),
              attributes
          );
        })
        .collect(Collectors.toList());

    return new FeedDto(
        feed.getId(),
        feed.getCreatedAt(),
        feed.getUpdatedAt(),
        authorDto,
        null,
        ootdItemDtos,
        feed.getContent(),
        0L, 0, false
    );
  }
}
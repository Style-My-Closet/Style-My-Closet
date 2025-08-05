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

  /**
   * 피드 생성 요청을 받아 새로운 피드를 생성하고, 관련 의류 및 날씨 정보를 연결한 후 피드 DTO로 반환합니다.
   *
   * 요청된 작성자, 의류, 날씨 정보를 검증하며, 존재하지 않는 경우 각각 USER_NOT_FOUND, CLOTHES_NOT_FOUND, WEATHER_NOT_FOUND 예외를 발생시킵니다.
   *
   * @param request 피드 생성에 필요한 작성자, 의류, 날씨, 내용 정보를 담은 요청 객체
   * @return 생성된 피드의 정보를 담은 FeedDto 객체
   * @throws StyleMyClosetException 작성자, 의류, 또는 날씨 정보가 존재하지 않을 경우 발생
   */
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

    Feed newFeed = Feed.builder()
        .author(author)
        .weather(weather)
        .content(request.content())
        .build();
    feedRepository.save(newFeed);

    List<FeedClothes> feedClothesList = clothesList.stream()
        .map(cloth -> FeedClothes.builder().feed(newFeed).clothes(cloth).build())
        .collect(Collectors.toList());
    feedClothesRepository.saveAll(feedClothesList);

    return mapToFeedResponse(newFeed, clothesList);
  }

  /**
   * 주어진 weatherId로 Weather 엔티티를 조회하며, weatherId가 null이면 null을 반환합니다.
   *
   * @param weatherId 조회할 Weather의 ID, null일 수 있음
   * @return 해당 ID의 Weather 엔티티 또는 weatherId가 null인 경우 null
   * @throws StyleMyClosetException weatherId가 null이 아니고, 해당 Weather 엔티티가 존재하지 않을 때 발생
   */
  private Weather findWeatherOrNull(Long weatherId) {
    if (weatherId == null) {
      return null;
    }
    return weatherRepository.findById(weatherId)
        .orElseThrow(() -> new StyleMyClosetException(ErrorCode.WEATHER_NOT_FOUND,
            Map.of("weatherId", weatherId)));
  }

  /**
   * Feed 및 관련 의류 목록을 FeedDto로 매핑합니다.
   *
   * @param feed       매핑할 피드 엔티티
   * @param clothesList 피드에 포함된 의류 엔티티 목록
   * @return 피드 정보, 작성자, OOTD 아이템 목록 등이 포함된 FeedDto 객체
   */
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
              ClothesType.valueOf(cloth.getCategory().getName().toUpperCase()),
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
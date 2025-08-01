package com.stylemycloset.ootd.service;

import com.stylemycloset.cloth.entity.Cloth;
import com.stylemycloset.cloth.repo.ClothRepository;
import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.controller.exception.StyleMyClosetException;
import com.stylemycloset.ootd.dto.AuthorDto;
import com.stylemycloset.ootd.dto.ClothesAttributeWithDefDto;
import com.stylemycloset.ootd.dto.FeedCreateRequest;
import com.stylemycloset.ootd.dto.FeedDto;
import com.stylemycloset.ootd.dto.OotdItemDto;
import com.stylemycloset.ootd.dto.PrecipitationDto;
import com.stylemycloset.ootd.dto.TemperatureDto;
import com.stylemycloset.ootd.entity.Feed;
import com.stylemycloset.ootd.entity.FeedClothes;
import com.stylemycloset.ootd.repo.FeedClothesRepository;
import com.stylemycloset.ootd.repo.FeedRepository;
import com.stylemycloset.ootd.tempEnum.ClothesType;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repo.UserRepository;
import com.stylemycloset.weather.dto.WeatherSummaryDto;
import com.stylemycloset.weather.entity.Weather;
import com.stylemycloset.weather.repo.WeatherRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // Feed 엔티티를 생성하고 DB에 먼저 저장
    Feed newFeed = Feed.builder()
        .author(author)
        .weather(weather)
        .content(request.content())
        .build();
    feedRepository.save(newFeed);

    // 각 Cloth와 Feed를 연결하는 FeedClothes 엔티티를 생성하고 저장
    List<FeedClothes> feedClothesList = clothesList.stream()
        .map(cloth -> FeedClothes.builder().feed(newFeed).clothes(cloth).build())
        .collect(Collectors.toList());
    feedClothesRepository.saveAll(feedClothesList);

    // 저장된 엔티티를 바탕으로 최종 응답 DTO를 만들어 반환
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
    // TODO: 다른 팀원 엔티티 완성되면 채울 예정
    // User 엔티티에서 필요한 정보를 꺼내 AuthorDto 생성
    AuthorDto authorDto = new AuthorDto(
        feed.getAuthor().getId(),
        feed.getAuthor().getName(),
        null // TODO: User 엔티티에 profileImageUrl 이 있다면 추가
    );

    // Weather 엔티티에서 필요한 정보를 꺼내 weatherSummaryDto 생성
    WeatherSummaryDto weatherDto = null;
    if (feed.getWeather() != null) {
      Weather weather = feed.getWeather();
      // TODO: Precipitation, Temperature DTO 변환 로직 추가 필요
      PrecipitationDto precipitationDto = new PrecipitationDto(
          weather.getPrecipitation().getType(),
          weather.getPrecipitation().getAmount(),
          weather.getPrecipitation().getProbability()
      );

      TemperatureDto temperatureDto = new TemperatureDto(
          weather.getTemperature().getCurrent(),
          weather.getTemperature().getComparedToDayBefore(),
          weather.getTemperature().getMin(),
          weather.getTemperature().getMax()
      );

      weatherDto = new WeatherSummaryDto(
          weather.getId(),
          weather.getSkyStatus(),
          precipitationDto,
          temperatureDto
      );
    }

    List<OotdItemDto> ootdItemDtos = clothesList.stream()
        .map(cloth -> {
          // TODO: cloth.getAttributeValues()를 분석해서 아래 attributes 리스트를 채워야 함
          // 지금은 빌드를 위해 임시로 비어있는 리스트를 사용
          List<ClothesAttributeWithDefDto> attributes = new ArrayList<>();

          return new OotdItemDto(
              cloth.getClothId(),
              cloth.getName(),
              null, // TODO: Cloth 엔티티에 imageUrl 필드가 추가되면 여기에 연결
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
        weatherDto,
        ootdItemDtos,
        feed.getContent(),
        0L, // 처음 생성 시 좋아요 수는 0
        0,  // 처음 생성 시 댓글 수는 0
        false // 처음 생성 시 나는 좋아요를 누르지 않음
    );
  }
}

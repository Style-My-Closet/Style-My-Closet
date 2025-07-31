package com.stylemycloset.ootd.service;

import com.stylemycloset.cloth.entity.Cloth;
import com.stylemycloset.common.controller.exception.ErrorCode;
import com.stylemycloset.common.controller.exception.StyleMyClosetException;
import com.stylemycloset.ootd.dto.FeedCreateRequest;
import com.stylemycloset.ootd.dto.FeedDto;
import com.stylemycloset.ootd.entity.Feed;
import com.stylemycloset.ootd.entity.FeedClothes;
import com.stylemycloset.ootd.repo.FeedClothesRepository;
import com.stylemycloset.ootd.repo.FeedRepository;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.weather.entity.Weather;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FeedServiceImpl implements FeedService {

  private final FeedRepository feedRepository;
  private final FeedClothesRepository feedClothesRepostiroy;
  private final UserRepository userRepository;
  private final ClothRepository clothRepository;
  private final WeatherRepository weatherRepository;

  public FeedDto createFeed(FeedCreateRequest request) {
    User author = userRepository.findById(request.authorId())
        .orElseThrow(()-> new StyleMyClosetException(ErrorCode.USER_NOT_FOUND,
            Map.of("userId", request.authorId())));

    Weather weather = findWeatherOrNull(request.weatherId());

    List<Cloth> clothesList = clothRepository.findAllById(request.clothesIds());
    if (clothesList.size() != request.clothesIds().size()) {
      throw new StyleMyClosetException(ErrorCode.CLOTHES_NOT_FOUND,
          Map.of("requestedIds", request.clothesIds()));
    }

    // Feed 엔티티를 생성하고 DB에 먼저 저장
    Feed feed = Feed.builder()
        .author(author)
        .weather(weather)
        .content(request.content())
        .build();
    feedRepository.save(newFeed);

    // 각 Cloth와 Feed를 연결하는 FeedClothes 엔티티를 생성하고 저장
    List<FeedClothes> feedClothesList = clothesList.stream()
        .map(cloth -> FeedClothes.builder().feed(newFeed).clothes(cloth).build())
        .collect(Collectors.toList());
    feedClothesRepostiroy.saveAll(feedClothesList);

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

  private FeedDto mapToFeedResponse(Feed feed, List<Cloth> clothList) {
    // TODO: 다른 팀원 엔티티 완성되면 채울 예정
    return new FeedDto(/* ... */);
  }

}

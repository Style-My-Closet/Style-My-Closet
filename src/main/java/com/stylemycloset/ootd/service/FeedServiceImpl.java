package com.stylemycloset.ootd.service;

import com.stylemycloset.cloth.entity.AttributeOption;
import com.stylemycloset.cloth.entity.Cloth;
import com.stylemycloset.cloth.entity.ClothingAttribute;
import com.stylemycloset.cloth.repository.ClothRepository;
import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import com.stylemycloset.notification.event.domain.FeedCommentEvent;
import com.stylemycloset.notification.event.domain.FeedLikedEvent;
import com.stylemycloset.notification.event.domain.NewFeedEvent;
import com.stylemycloset.ootd.dto.AuthorDto;
import com.stylemycloset.ootd.dto.ClothesAttributeWithDefDto;
import com.stylemycloset.ootd.dto.CommentCreateRequest;
import com.stylemycloset.ootd.dto.CommentCursorResponse;
import com.stylemycloset.ootd.dto.CommentDto;
import com.stylemycloset.ootd.dto.CommentSearchRequest;
import com.stylemycloset.ootd.dto.FeedCreateRequest;
import com.stylemycloset.ootd.dto.FeedDto;
import com.stylemycloset.ootd.dto.FeedDtoCursorResponse;
import com.stylemycloset.ootd.dto.FeedSearchRequest;
import com.stylemycloset.ootd.dto.FeedUpdateRequest;
import com.stylemycloset.ootd.dto.OotdItemDto;
import com.stylemycloset.ootd.entity.Feed;
import com.stylemycloset.ootd.entity.FeedClothes;
import com.stylemycloset.ootd.entity.FeedComment;
import com.stylemycloset.ootd.entity.FeedLike;
import com.stylemycloset.ootd.repo.FeedClothesRepository;
import com.stylemycloset.ootd.repo.FeedCommentRepository;
import com.stylemycloset.ootd.repo.FeedLikeRepository;
import com.stylemycloset.ootd.repo.FeedRepository;
import com.stylemycloset.ootd.tempEnum.ClothesType;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import com.stylemycloset.weather.dto.PrecipitationDto;
import com.stylemycloset.weather.dto.TemperatureDto;
import com.stylemycloset.weather.dto.WeatherSummaryDto;
import com.stylemycloset.weather.entity.Weather;
import com.stylemycloset.weather.repository.WeatherRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedServiceImpl implements FeedService {

  private final FeedRepository feedRepository;
  private final FeedClothesRepository feedClothesRepository;
  private final UserRepository userRepository;
  private final ClothRepository clothRepository;
  private final WeatherRepository weatherRepository;
  private final FeedLikeRepository feedLikeRepository;
  private final FeedCommentRepository feedCommentRepository;
  private final ApplicationEventPublisher publisher;

  @Override
  @Transactional
  public FeedDto createFeed(FeedCreateRequest request) {
    User author = userRepository.findById(request.authorId())
        .orElseThrow(() -> new StyleMyClosetException(ErrorCode.USER_NOT_FOUND,
            Map.of("id", request.authorId())));

    Weather weather = findWeatherOrNull(request.weatherId());

    List<Cloth> clothesList = clothRepository.findAllById(request.clothesIds());
    if (clothesList.size() != request.clothesIds().size()) {
      throw new StyleMyClosetException(ErrorCode.CLOTHES_NOT_FOUND,
          Map.of("requestedIds", request.clothesIds()));
    }

    Feed newFeed = Feed.createFeed(author, weather, request.content());

    clothesList.forEach(newFeed::addClothes);

    feedRepository.save(newFeed);

    publisher.publishEvent(
        new NewFeedEvent(newFeed.getId(), newFeed.getContent(), newFeed.getAuthor().getId()));

    return mapToFeedResponse(newFeed, author);
  }

  @Override
  public FeedDtoCursorResponse getFeeds(FeedSearchRequest request) {
    // TODO: 이 메서드에도 현재 로그인한 유저 ID를 파라미터로 받아와야 likedByMe를 계산
    User currentUser = null;

    List<Feed> feeds = feedRepository.findByConditions(request);

    int limit = request.limit() != null ? request.limit() : 10;
    boolean hasNext = feeds.size() > limit;
    if (hasNext) {
      feeds.remove(limit);
    }

    String nextCursor = null;
    Long nextIdAfter = null;
    if (hasNext && !feeds.isEmpty()) {
      Feed lastFeed = feeds.get(feeds.size() - 1);
      if ("createdAt".equals(request.sortBy()) || request.sortBy() == null) {
        nextCursor = lastFeed.getCreatedAt().toString();
      }

      nextIdAfter = lastFeed.getId();
    }

    // 좋아요 수와 상태를 Batch 쿼리로 조회
    Map<Long, Long> likeCountMap = getLikeCountMap(feeds);
    Map<Long, Boolean> likedByMeMap = currentUser != null ? getLikedByMeMap(feeds, currentUser) : new HashMap<>();

    List<FeedDto> feedDtos = feeds.stream()
        .map(feed -> mapToFeedResponseWithLikeInfo(feed, currentUser, likeCountMap, likedByMeMap))
        .collect(Collectors.toList());

    return new FeedDtoCursorResponse(
        feedDtos, nextCursor, nextIdAfter, hasNext, 0L, request.sortBy(), request.sortDirection());
  }

  @Override
  @Transactional
  public FeedDto toggleLike(Long userId, Long feedId) {
    User user = userRepository.findByIdAndDeletedAtIsNullAndLockedIsFalse(userId)
        .orElseThrow(() -> new StyleMyClosetException(ErrorCode.USER_NOT_FOUND,
            Map.of("userId", userId)));

    Feed feed = feedRepository.findById(feedId)
        .orElseThrow(() -> new StyleMyClosetException(ErrorCode.FEED_NOT_FOUND,
            Map.of("feedId", feedId)));

    Optional<FeedLike> existingLike = feedLikeRepository.findByUserAndFeed(user, feed);

    if (existingLike.isPresent()) {
      // 이미 좋아요가 존재하면 -> 삭제 (좋아요 취소)
      feedLikeRepository.delete(existingLike.get());
    } else {
      // 좋아요가 없으면 -> 생성 (좋아요)
      FeedLike newLike = FeedLike.createFeedLike(user, feed);
      feedLikeRepository.save(newLike);
      publisher.publishEvent(new FeedLikedEvent(feed.getId(), userId));
    }

    return mapToFeedResponse(feed, user);
  }

  private Weather findWeatherOrNull(Long weatherId) {
    if (weatherId == null) {
      return null;
    }
    return weatherRepository.findById(weatherId)
        .orElseThrow(() -> new StyleMyClosetException(ErrorCode.WEATHER_NOT_FOUND,
            Map.of("weatherId", weatherId)));
  }

  private FeedDto mapToFeedResponse(Feed feed, User currentUser) {
    List<Cloth> clothesList = feed.getFeedClothes().stream()
        .map(FeedClothes::getClothes)
        .collect(Collectors.toList());

    AuthorDto authorDto = toAuthorDto(feed.getAuthor());
    WeatherSummaryDto weatherDto = toWeatherSummaryDto(feed.getWeather());
    List<OotdItemDto> ootdItemDtos = toOotdItemDtoList(clothesList);

    // Batch 쿼리로 좋아요 정보 조회
    List<Long> feedIds = List.of(feed.getId());
    Map<Long, Long> likeCountMap = getLikeCountMapByFeedIds(feedIds);
    Map<Long, Boolean> likedByMeMap = currentUser != null ? getLikedByMeMapByFeedIds(feedIds, currentUser) : new HashMap<>();

    long likeCount = likeCountMap.getOrDefault(feed.getId(), 0L);
    boolean likedByMe = likedByMeMap.getOrDefault(feed.getId(), false);

    return new FeedDto(
        feed.getId(),
        feed.getCreatedAt(),
        feed.getUpdatedAt(),
        authorDto,
        weatherDto,
        ootdItemDtos,
        feed.getContent(),
        likeCount,
        0,  // TODO: 댓글 수 계산 로직 추가 필요
        likedByMe
    );
  }

  private FeedDto mapToFeedResponseWithLikeInfo(Feed feed, User currentUser, 
      Map<Long, Long> likeCountMap, Map<Long, Boolean> likedByMeMap) {
    List<Cloth> clothesList = feed.getFeedClothes().stream()
        .map(FeedClothes::getClothes)
        .collect(Collectors.toList());

    AuthorDto authorDto = toAuthorDto(feed.getAuthor());
    WeatherSummaryDto weatherDto = toWeatherSummaryDto(feed.getWeather());
    List<OotdItemDto> ootdItemDtos = toOotdItemDtoList(clothesList);

    long likeCount = likeCountMap.getOrDefault(feed.getId(), 0L);
    boolean likedByMe = likedByMeMap.getOrDefault(feed.getId(), false);

    return new FeedDto(
        feed.getId(),
        feed.getCreatedAt(),
        feed.getUpdatedAt(),
        authorDto,
        weatherDto,
        ootdItemDtos,
        feed.getContent(),
        likeCount,
        0,  // TODO: 댓글 수 계산 로직 추가 필요
        likedByMe
    );
  }

  private Map<Long, Long> getLikeCountMap(List<Feed> feeds) {
    List<Long> feedIds = feeds.stream().map(Feed::getId).collect(Collectors.toList());
    if (feedIds.isEmpty()) {
      return new HashMap<>();
    }
    
    // Batch 쿼리로 좋아요 수 조회
    List<Object[]> results = feedLikeRepository.countByFeedIds(feedIds);
    return results.stream()
        .collect(Collectors.toMap(
            result -> (Long) result[0],
            result -> (Long) result[1]
        ));
  }

  private Map<Long, Boolean> getLikedByMeMap(List<Feed> feeds, User currentUser) {
    List<Long> feedIds = feeds.stream().map(Feed::getId).collect(Collectors.toList());
    if (feedIds.isEmpty()) {
      return new HashMap<>();
    }
    
    // Batch 쿼리로 좋아요 상태 조회
    List<Long> likedFeedIds = feedLikeRepository.findFeedIdsByUserAndFeedIds(currentUser.getId(), feedIds);
    return feedIds.stream()
        .collect(Collectors.toMap(
            feedId -> feedId,
            feedId -> likedFeedIds.contains(feedId)
        ));
  }

  private AuthorDto toAuthorDto(User author) {
    if (author == null) {
      return null;
    }
    return new AuthorDto(author.getId(), author.getName(), null);
  }

  private WeatherSummaryDto toWeatherSummaryDto(Weather weather) {
    if (weather == null) {
      return null;
    }
    PrecipitationDto precipitationDto = new PrecipitationDto(
        Weather.AlertType.valueOf(weather.getPrecipitation().getAlertType().name().toUpperCase()),
        weather.getPrecipitation().getAmount(),
        weather.getPrecipitation().getProbability()
    );
    TemperatureDto temperatureDto = new TemperatureDto(
        weather.getTemperature().getCurrent(),
        weather.getTemperature().getComparedToDayBefore(),
        weather.getTemperature().getMin(),
        weather.getTemperature().getMax()
    );

    return new WeatherSummaryDto(weather.getId(), weather.getSkyStatus(), precipitationDto,
        temperatureDto);
  }

  private List<OotdItemDto> toOotdItemDtoList(List<Cloth> clothesList) {
    return clothesList.stream().map(this::toOotdItemDto).collect(Collectors.toList());
  }

  private OotdItemDto toOotdItemDto(Cloth cloth) {
    List<ClothesAttributeWithDefDto> attributes = cloth.getAttributeValues().stream()
        .map(attributeValue -> {
          ClothingAttribute definition = attributeValue.getAttribute(); // 속성의 정의

          // 해당 속성이 가질 수 있는 모든 선택지
          List<String> selectableValues = definition.getOptions().stream()
              .map(AttributeOption::getValue)
              .collect(Collectors.toList());

          // 이 옷이 선택한 특정 값을 가져옴
          String chosenValue = attributeValue.getOption().getValue();

          return new ClothesAttributeWithDefDto(
              definition.getId(),
              definition.getName(),
              selectableValues,
              chosenValue
          );
        })
        .collect(Collectors.toList());

    return new OotdItemDto(cloth.getId(), cloth.getName(), null, // TODO: 이미지 URL 로직
        ClothesType.valueOf(cloth.getCategory().getName().name()), attributes);
  }

  @Override
  @Transactional
  public FeedDto updateFeed(Long currentUserId, Long feedId, FeedUpdateRequest request) {
    Feed feed = feedRepository.findById(feedId)
        .orElseThrow(() -> new StyleMyClosetException(ErrorCode.FEED_NOT_FOUND,
            Map.of("feedId", feedId)));

    if (!feed.getAuthor().getId().equals(currentUserId)) {
      throw new StyleMyClosetException(ErrorCode.ERROR_CODE, Map.of("reason", "수정 권한이 없습니다."));
    }

    feed.updateContent(request.content());

    User currentUser = userRepository.findById(currentUserId)
        .orElseThrow(() -> new StyleMyClosetException(ErrorCode.USER_NOT_FOUND,
            Map.of("userId", currentUserId)));

    return mapToFeedResponse(feed, currentUser);
  }

  @Override
  @Transactional
  public void deleteFeed(Long currentUserId, Long feedId) {
    // 삭제할 피드를 조회
    Feed feed = feedRepository.findById(feedId)
        .orElseThrow(() -> new StyleMyClosetException(ErrorCode.FEED_NOT_FOUND,
            Map.of("feedId", feedId)));

    // 권환 확인
    if (!feed.getAuthor().getId().equals(currentUserId)) {
      throw new StyleMyClosetException(ErrorCode.ERROR_CODE, Map.of("reason", "삭제 권한이 없습니다."));
    }

    feedRepository.delete(feed);
  }

  @Override
  public CommentCursorResponse getComments(Long feedId, CommentSearchRequest request) {
    if (!feedRepository.existsById(feedId)) {
      throw new StyleMyClosetException(ErrorCode.FEED_NOT_FOUND, Map.of("feedId", feedId));
    }

    List<FeedComment> comments = feedCommentRepository.findByFeedIdWithCursor(feedId, request);

    int limit = request.limit();
    boolean hasNext = comments.size() > limit;
    if (hasNext) {
      comments.remove(limit);
    }

    String nextCursor = null;
    Long nextIdAfter = null;
    if (hasNext && !comments.isEmpty()) {
      FeedComment lastComment = comments.get(comments.size() - 1);
      nextCursor = lastComment.getCreatedAt().toString();
      nextIdAfter = lastComment.getId();
    }

    List<CommentDto> commentDtos = comments.stream()
        .map(this::toCommentDto) // DTO 변환
        .collect(Collectors.toList());

    // TODO: totalCount 로직 추가 필요
    return new CommentCursorResponse(commentDtos, nextCursor, nextIdAfter, hasNext, 0L, "createdAt",
        "DESC");
  }

  @Override
  @Transactional
  public CommentDto createComment(CommentCreateRequest request, Long currentUserId) {
    if (!request.authorId().equals(currentUserId)) {
      throw new StyleMyClosetException(ErrorCode.ERROR_CODE, Map.of("reason", "댓글을 작성할 권한이 없습니다."));
    }

    User author = userRepository.findByIdAndDeletedAtIsNullAndLockedIsFalse(request.authorId())
        .orElseThrow(() -> new StyleMyClosetException(ErrorCode.USER_NOT_FOUND,
            Map.of("userId", request.authorId())));

    Feed feed = feedRepository.findById(request.feedId())
        .orElseThrow(() -> new StyleMyClosetException(ErrorCode.FEED_NOT_FOUND,
            Map.of("feedId", request.feedId())));

    FeedComment newComment = new FeedComment(feed, author, request.content());
    FeedComment savedComment = feedCommentRepository.save(newComment);

    publisher.publishEvent(new FeedCommentEvent(newComment.getId(), newComment.getAuthor().getId()));

    return toCommentDto(savedComment);
  }

  private CommentDto toCommentDto(FeedComment comment) {
    return new CommentDto(
        comment.getId(),
        comment.getCreatedAt(),
        comment.getFeed().getId(),
        toAuthorDto(comment.getAuthor()),
        comment.getContent()
    );
  }

  // Feed ID 리스트로 좋아요 수 조회 (개별 피드용)
  private Map<Long, Long> getLikeCountMapByFeedIds(List<Long> feedIds) {
    if (feedIds.isEmpty()) {
      return new HashMap<>();
    }
    
    // 모든 피드에 대해 기본값 0 설정
    Map<Long, Long> likeCountMap = feedIds.stream()
        .collect(Collectors.toMap(feedId -> feedId, feedId -> 0L));
    
    // Batch 쿼리로 좋아요 수 조회
    List<Object[]> results = feedLikeRepository.countByFeedIds(feedIds);
    results.forEach(result -> {
      Long feedId = (Long) result[0];
      Long count = (Long) result[1];
      likeCountMap.put(feedId, count);
    });
    
    return likeCountMap;
  }

  // Feed ID 리스트로 좋아요 상태 조회 (개별 피드용)
  private Map<Long, Boolean> getLikedByMeMapByFeedIds(List<Long> feedIds, User currentUser) {
    if (feedIds.isEmpty()) {
      return new HashMap<>();
    }
    
    // Batch 쿼리로 좋아요 상태 조회
    List<Long> likedFeedIds = feedLikeRepository.findFeedIdsByUserAndFeedIds(currentUser.getId(), feedIds);
    return feedIds.stream()
        .collect(Collectors.toMap(
            feedId -> feedId,
            feedId -> likedFeedIds.contains(feedId)
        ));
  }
}
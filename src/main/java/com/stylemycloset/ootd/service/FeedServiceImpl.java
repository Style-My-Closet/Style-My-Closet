package com.stylemycloset.ootd.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stylemycloset.clothes.entity.clothes.Clothes;
import com.stylemycloset.clothes.repository.clothes.ClothesRepository;
import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import com.stylemycloset.notification.event.domain.FeedCommentEvent;
import com.stylemycloset.notification.event.domain.FeedLikedEvent;
import com.stylemycloset.notification.event.domain.NewFeedEvent;
import com.stylemycloset.ootd.dto.CommentCreateRequest;
import com.stylemycloset.ootd.dto.CommentCursorResponse;
import com.stylemycloset.ootd.dto.CommentDto;
import com.stylemycloset.ootd.dto.CommentSearchRequest;
import com.stylemycloset.ootd.dto.FeedCreateRequest;
import com.stylemycloset.ootd.dto.FeedDto;
import com.stylemycloset.ootd.dto.FeedDtoCursorResponse;
import com.stylemycloset.ootd.dto.FeedSearchRequest;
import com.stylemycloset.ootd.dto.FeedUpdateRequest;
import com.stylemycloset.ootd.entity.Feed;
import com.stylemycloset.ootd.entity.FeedComment;
import com.stylemycloset.ootd.entity.FeedLike;
import com.stylemycloset.ootd.mapper.CommentMapper;
import com.stylemycloset.ootd.mapper.FeedMapper;

import com.stylemycloset.ootd.repo.FeedCommentRepository;
import com.stylemycloset.ootd.repo.FeedLikeRepository;
import com.stylemycloset.ootd.repo.FeedRepository;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import com.stylemycloset.weather.entity.Weather;
import com.stylemycloset.weather.repository.WeatherRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedServiceImpl implements FeedService {

  private final FeedRepository feedRepository;

  private final UserRepository userRepository;
  private final ClothesRepository clothRepository;
  private final WeatherRepository weatherRepository;
  private final FeedLikeRepository feedLikeRepository;
  private final FeedCommentRepository feedCommentRepository;
  private final ApplicationEventPublisher publisher;
  private final FeedMapper feedMapper;
  private final CommentMapper commentMapper;

  @Override
  @Transactional
  public FeedDto createFeed(FeedCreateRequest request) {
    User author = userRepository.findById(request.authorId())
        .orElseThrow(() -> new StyleMyClosetException(ErrorCode.USER_NOT_FOUND,
            Map.of("id", request.authorId())));

    Weather weather = findWeatherOrNull(request.weatherId());

    List<Clothes> clothesList = clothRepository.findAllById(request.clothesIds());
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
  public FeedDtoCursorResponse getFeeds(FeedSearchRequest request, @Nullable Long currentUserId) {
    // 인증된 사용자 정보 조회
    User currentUser = currentUserId != null ?
        userRepository.findByIdAndDeletedAtIsNullAndLockedIsFalse(currentUserId).orElse(null) : null;

    List<Feed> feeds = feedRepository.findByConditions(request);

    int limit = request.limit() != null ? request.limit() : 10;
    boolean hasNext = feeds.size() > limit;
    if (hasNext) {
      feeds.remove(limit);
    }

    // 좋아요 수와 상태를 Batch 쿼리로 조회
    Map<Long, Long> likeCountMap = getLikeCountMap(feeds);
    Map<Long, Boolean> likedByMeMap = currentUser != null ? getLikedByMeMap(feeds, currentUser) : new HashMap<>();

    String nextCursor = null;
    Long nextIdAfter = null;
    if (hasNext && !feeds.isEmpty()) {
      Feed lastFeed = feeds.get(feeds.size() - 1);
      if ("createdAt".equals(request.sortBy()) || request.sortBy() == null) {
        nextCursor = lastFeed.getCreatedAt().toString();
      } else if ("likeCount".equals(request.sortBy())) {
        // likeCount 정렬 시 좋아요 수를 cursor로 사용
        nextCursor = String.valueOf(likeCountMap.getOrDefault(lastFeed.getId(), 0L));
      }

      nextIdAfter = lastFeed.getId();
    }

    // FeedMapper를 사용하여 FeedDto 리스트 생성
    List<FeedDto> feedDtos = feedMapper.toDtoList(feeds, currentUser, likeCountMap,
                        new HashMap<>(), likedByMeMap);

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
    // Batch 쿼리로 좋아요 정보 조회
    List<Long> feedIds = List.of(feed.getId());
    Map<Long, Long> likeCountMap = getLikeCountMapByFeedIds(feedIds);
    Map<Long, Boolean> likedByMeMap = currentUser != null ? getLikedByMeMapByFeedIds(feedIds, currentUser) : new HashMap<>();

    long likeCount = likeCountMap.getOrDefault(feed.getId(), 0L);
    boolean likedByMe = likedByMeMap.getOrDefault(feed.getId(), false);

    // FeedMapper를 사용하여 FeedDto 생성
    return feedMapper.toDto(feed, currentUser, likeCount, 0, likedByMe);
  }



  private Map<Long, Long> getLikeCountMap(List<Feed> feeds) {
    List<Long> feedIds = feeds.stream().map(Feed::getId).collect(Collectors.toList());
    if (feedIds.isEmpty()) {
      return new HashMap<>();
    }

    // Batch 쿼리로 좋아요 수 조회
    List<FeedLikeRepository.FeedLikeCountProjection> results = feedLikeRepository.countByFeedIds(feedIds);
    return results.stream()
        .collect(Collectors.toMap(
            FeedLikeRepository.FeedLikeCountProjection::getFeedId,
            FeedLikeRepository.FeedLikeCountProjection::getLikeCount
        ));
  }

  private Map<Long, Boolean> getLikedByMeMap(List<Feed> feeds, User currentUser) {
    List<Long> feedIds = feeds.stream().map(Feed::getId).collect(Collectors.toList());
    if (feedIds.isEmpty()) {
      return new HashMap<>();
    }

    // Batch 쿼리로 좋아요 상태 조회
    List<Long> likedFeedIds = feedLikeRepository.findFeedIdsByUserAndFeedIds(currentUser.getId(), feedIds);
    HashSet<Long> likedFeedIdSet = new HashSet<>(likedFeedIds); // O(1)
    return feedIds.stream()
        .collect(Collectors.toMap(
            feedId -> feedId,
            feedId -> likedFeedIdSet.contains(feedId) // O(1)
        ));
  }

  @Override
  @Transactional
  public FeedDto updateFeed(Long currentUserId, Long feedId, FeedUpdateRequest request) {
    Feed feed = feedRepository.findById(feedId)
        .orElseThrow(() -> new StyleMyClosetException(ErrorCode.FEED_NOT_FOUND,
            Map.of("feedId", feedId)));

    if (!feed.getAuthor().getId().equals(currentUserId)) {
      throw new StyleMyClosetException(ErrorCode.FEED_UPDATE_FORBIDDEN, Map.of("feedId", feedId, "currentUserId", currentUserId));
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

    // 권한 확인
    if (!feed.getAuthor().getId().equals(currentUserId)) {
      throw new StyleMyClosetException(ErrorCode.FEED_DELETE_FORBIDDEN, Map.of("feedId", feedId, "currentUserId", currentUserId));
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

    // CommentMapper를 사용하여 CommentDto 리스트 생성
    List<CommentDto> commentDtos = commentMapper.toDtoList(comments);

    return new CommentCursorResponse(commentDtos, nextCursor, nextIdAfter, hasNext, 0L, "createdAt",
        "DESC");
  }

  @Override
  @Transactional
  public CommentDto createComment(CommentCreateRequest request, Long currentUserId) {
    if (!request.authorId().equals(currentUserId)) {
      throw new StyleMyClosetException(ErrorCode.COMMENT_CREATE_FORBIDDEN, Map.of("requestAuthorId", request.authorId(), "currentUserId", currentUserId));
    }

    User author = userRepository.findByIdAndDeletedAtIsNullAndLockedIsFalse(request.authorId())
        .orElseThrow(() -> new StyleMyClosetException(ErrorCode.USER_NOT_FOUND,
            Map.of("userId", request.authorId())));

    Feed feed = feedRepository.findById(request.feedId())
        .orElseThrow(() -> new StyleMyClosetException(ErrorCode.FEED_NOT_FOUND,
            Map.of("feedId", request.feedId())));

    FeedComment newComment = new FeedComment(feed, author, request.content());
    FeedComment savedComment = feedCommentRepository.save(newComment);

    publisher.publishEvent(new FeedCommentEvent(feed.getId(), author.getId()));

    return commentMapper.toDto(savedComment);
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
    List<FeedLikeRepository.FeedLikeCountProjection> results = feedLikeRepository.countByFeedIds(feedIds);
    results.forEach(result -> {
      Long feedId = result.getFeedId();
      Long count = result.getLikeCount();
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
    HashSet<Long> likedFeedIdSet = new HashSet<>(likedFeedIds); // O(1)
    return feedIds.stream()
        .collect(Collectors.toMap(
            feedId -> feedId,
            feedId -> likedFeedIdSet.contains(feedId) // O(1)
        ));
  }
}

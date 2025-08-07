package com.stylemycloset.ootd.controller;

import com.stylemycloset.ootd.dto.FeedCreateRequest;
import com.stylemycloset.ootd.dto.FeedDto;
import com.stylemycloset.ootd.dto.FeedDtoCursorResponse;
import com.stylemycloset.ootd.service.FeedService;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import com.stylemycloset.weather.entity.Weather.SkyStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedController {

  private final FeedService feedService;
  private final UserRepository userRepository;

  @PostMapping
  public ResponseEntity<FeedDto> createFeed(@Valid @RequestBody FeedCreateRequest request) {
    // TODO: 실제로는 Spring Security를 통해 로그인한 사용자 ID를 가져와야 함
    // 지금은 request 에 있는 authorID를 그대로 사용
    FeedDto responseDto = feedService.createFeed(request);

    return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
  }

  @GetMapping
  public ResponseEntity<FeedDtoCursorResponse> getFeeds(
      @RequestParam(required = false) Long cursorId,
      @PageableDefault(size = 10) Pageable pageable,
      @RequestParam(required = false) String keywordLike,
      @RequestParam(required = false) SkyStatus skyStatusEqual,
      @RequestParam(required = false) Long authorIdEqual

  ) {
    FeedDtoCursorResponse response = feedService.getFeeds(cursorId, keywordLike, skyStatusEqual, authorIdEqual, pageable);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{feedId}/like")
  public ResponseEntity<FeedDto> likeFeed(@PathVariable Long feedId, Authentication authentication) {
    User user = userRepository.findByEmail(authentication.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    Long currentUserId = user.getId();
    FeedDto responseDto = feedService.likeFeed(currentUserId, feedId);
    return ResponseEntity.ok(responseDto);
  }

  @DeleteMapping("/{feedId}/like")
  public ResponseEntity<Void> unlikeFeed(@PathVariable Long feedId, Authentication authentication) {
    User user = userRepository.findByEmail(authentication.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    Long currentUserId = user.getId();
    feedService.unlikeFeed(currentUserId, feedId);
    return ResponseEntity.noContent().build();
  }
}

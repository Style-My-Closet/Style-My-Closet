package com.stylemycloset.ootd.controller;

import com.stylemycloset.ootd.dto.CommentCreateRequest;
import com.stylemycloset.ootd.dto.CommentCursorResponse;
import com.stylemycloset.ootd.dto.CommentDto;
import com.stylemycloset.ootd.dto.CommentSearchRequest;
import com.stylemycloset.ootd.dto.FeedCreateRequest;
import com.stylemycloset.ootd.dto.FeedDto;
import com.stylemycloset.ootd.dto.FeedDtoCursorResponse;
import com.stylemycloset.ootd.dto.FeedSearchRequest;
import com.stylemycloset.ootd.dto.FeedUpdateRequest;
import com.stylemycloset.ootd.service.FeedService;
import com.stylemycloset.security.ClosetUserDetails;
import com.stylemycloset.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedController {

  private final FeedService feedService;
  private final UserRepository userRepository;

  @PostMapping
  public ResponseEntity<FeedDto> createFeed(
      @Valid @RequestBody FeedCreateRequest request,
      @AuthenticationPrincipal ClosetUserDetails principal
  ) {
    if (!principal.getUserId().equals(request.authorId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid authorId");
    }
    FeedDto responseDto = feedService.createFeed(request);

    return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
  }

  @GetMapping
  public ResponseEntity<FeedDtoCursorResponse> getFeeds(
      @Valid FeedSearchRequest request

  ) {

    FeedDtoCursorResponse response = feedService.getFeeds(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{feedId}/like")
  public ResponseEntity<FeedDto> likeFeed(
      @PathVariable Long feedId,
      @AuthenticationPrincipal ClosetUserDetails principal
  ) {
    FeedDto responseDto = feedService.toggleLike(principal.getUserId(), feedId);
    return ResponseEntity.ok(responseDto);
  }

  @DeleteMapping("/{feedId}/like")
  public ResponseEntity<Void> unlikeFeed(
      @PathVariable Long feedId,
      @AuthenticationPrincipal ClosetUserDetails principal) {
    feedService.toggleLike(principal.getUserId(), feedId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{feedId}/comments")
  public ResponseEntity<CommentCursorResponse> getComments(
      @PathVariable Long feedId,
      @Valid CommentSearchRequest request
  ) {
    CommentCursorResponse response = feedService.getComments(feedId, request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{feedId}/comments")
  public ResponseEntity<CommentDto> createComment(
      @PathVariable Long feedId,
      @Valid @RequestBody CommentCreateRequest request,
      @AuthenticationPrincipal ClosetUserDetails principal
  ) {
    if (!feedId.equals(request.feedId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid feedId");
    }
    CommentDto createdCommentDto = feedService.createComment(request, principal.getUserId());

    return ResponseEntity.status(HttpStatus.CREATED).body(createdCommentDto);
  }

  @DeleteMapping("/{feedId}")
  public ResponseEntity<Void> deleteFeed(
      @PathVariable Long feedId,
      @AuthenticationPrincipal ClosetUserDetails principal
  ) {
    feedService.deleteFeed(principal.getUserId(), feedId);

    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{feedId}")
  public ResponseEntity<FeedDto> updateFeed(
      @PathVariable Long feedId,
      @Valid @RequestBody FeedUpdateRequest request,
      @AuthenticationPrincipal ClosetUserDetails principal
  ) {
    FeedDto responseDto = feedService.updateFeed(principal.getUserId(), feedId, request);

    return ResponseEntity.ok(responseDto);
  }
}

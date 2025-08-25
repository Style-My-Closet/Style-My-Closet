package com.stylemycloset.ootd.controller;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedController {

  private final FeedService feedService;

  @PostMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<FeedDto> createFeed(
      @Valid @RequestBody FeedCreateRequest request,
      @AuthenticationPrincipal ClosetUserDetails userDetails
  ) {
    Long userId = userDetails.getUserId();
    if (!userId.equals(request.authorId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid authorId");
    }
    FeedDto responseDto = feedService.createFeed(request);

    return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
  }

  @GetMapping
  public ResponseEntity<FeedDtoCursorResponse> getFeeds(
      @Valid FeedSearchRequest request,
      @AuthenticationPrincipal ClosetUserDetails userDetails
  ) {
    Long currentUserId = userDetails != null ? userDetails.getUserId() : null;
    FeedDtoCursorResponse response = feedService.getFeeds(request, currentUserId);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{feedId}/like")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<FeedDto> likeFeed(
      @PathVariable Long feedId,
      @AuthenticationPrincipal ClosetUserDetails userDetails
  ) {
    Long userId = userDetails.getUserId();
    FeedDto responseDto = feedService.toggleLike(userId, feedId);
    return ResponseEntity.ok(responseDto);
  }

  @DeleteMapping("/{feedId}/like")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> unlikeFeed(
      @PathVariable Long feedId,
      @AuthenticationPrincipal ClosetUserDetails userDetails) {
    Long userId = userDetails.getUserId();
    feedService.toggleLike(userId, feedId);
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
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<CommentDto> createComment(
      @PathVariable Long feedId,
      @Valid @RequestBody CommentCreateRequest request,
      @AuthenticationPrincipal ClosetUserDetails userDetails
  ) {
    Long userId = userDetails.getUserId();
    if (!feedId.equals(request.feedId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid feedId");
    }
    CommentDto createdCommentDto = feedService.createComment(request, userId);

    return ResponseEntity.status(HttpStatus.CREATED).body(createdCommentDto);
  }

  @DeleteMapping("/{feedId}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> deleteFeed(
      @PathVariable Long feedId,
      @AuthenticationPrincipal ClosetUserDetails userDetails
  ) {
    Long userId = userDetails.getUserId();
    feedService.deleteFeed(userId, feedId);

    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{feedId}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<FeedDto> updateFeed(
      @PathVariable Long feedId,
      @Valid @RequestBody FeedUpdateRequest request,
      @AuthenticationPrincipal ClosetUserDetails userDetails
  ) {
    Long userId = userDetails.getUserId();
    FeedDto responseDto = feedService.updateFeed(userId, feedId, request);

    return ResponseEntity.ok(responseDto);
  }
}

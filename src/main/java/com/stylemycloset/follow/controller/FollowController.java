package com.stylemycloset.follow.controller;

import com.stylemycloset.follow.dto.FollowListResponse;
import com.stylemycloset.follow.dto.FollowResult;
import com.stylemycloset.follow.dto.FollowSummaryResult;
import com.stylemycloset.follow.dto.request.FollowCreateRequest;
import com.stylemycloset.follow.dto.request.SearchFollowersCondition;
import com.stylemycloset.follow.dto.request.SearchFollowingsCondition;
import com.stylemycloset.follow.service.FollowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

  private final FollowService followService;

  @PostMapping
  public ResponseEntity<FollowResult> startFollowing(
      @Valid @RequestBody FollowCreateRequest followCreateRequest
  ) {
    FollowResult followResult = followService.startFollowing(followCreateRequest);
    return ResponseEntity.ok(followResult);
  }

  @GetMapping("/summary")
  public ResponseEntity<FollowSummaryResult> getFollowSummaryResult(
      @RequestParam(value = "userId") Long userId,
      @AuthenticationPrincipal Long logInUserId // 시큐리티 추가시 넣을 예정
  ) {
    FollowSummaryResult followSummaryResult = followService.getFollowSummary(userId, logInUserId);
    return ResponseEntity.ok(followSummaryResult);
  }

  @GetMapping("/followings")
  public ResponseEntity<FollowListResponse<FollowResult>> getFollowings(
      @Valid @ModelAttribute SearchFollowingsCondition followingsCondition
  ) {
    FollowListResponse<FollowResult> followings = followService.getFollowings(followingsCondition);
    return ResponseEntity.ok(followings);
  }

  @GetMapping("/follower")
  public ResponseEntity<FollowListResponse<FollowResult>> getFollowers(
      @Valid @ModelAttribute SearchFollowersCondition followersCondition
  ) {
    FollowListResponse<FollowResult> followers = followService.getFollowers(followersCondition);
    return ResponseEntity.ok(followers);
  }

  @DeleteMapping("/{followId}")
  public ResponseEntity<Void> softDelete(@PathVariable("followId") Long followId) {
    followService.softDelete(followId);
    return ResponseEntity.noContent()
        .build();
  }

  @DeleteMapping("/{followId}/hard-delete")
  public ResponseEntity<Void> hardDelete(@PathVariable("followId") Long followId) {
    followService.hardDelete(followId);
    return ResponseEntity.noContent()
        .build();
  }

}

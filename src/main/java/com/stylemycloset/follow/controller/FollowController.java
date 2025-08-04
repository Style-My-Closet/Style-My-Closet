package com.stylemycloset.follow.controller;

import com.stylemycloset.follow.dto.FollowListResponse;
import com.stylemycloset.follow.dto.FollowResult;
import com.stylemycloset.follow.dto.FollowSummaryResult;
import com.stylemycloset.follow.dto.request.FollowCreateRequest;
import com.stylemycloset.follow.dto.request.SearchFollowersCondition;
import com.stylemycloset.follow.dto.request.SearchFollowingsCondition;
import com.stylemycloset.follow.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

  private final FollowService followService;

  @PostMapping("/api/follows")
  public ResponseEntity<FollowResult> startFollowing(FollowCreateRequest followCreateRequest) {
    FollowResult followResult = followService.startFollowing(followCreateRequest);
    return ResponseEntity.ok(followResult);
  }

  @GetMapping("/summary")
  public ResponseEntity<FollowSummaryResult> getFollowSummaryResult(
      @RequestParam(value = "userId") Long targetUserId,
      @AuthenticationPrincipal Long logInUserId // 시큐리티 구현시 추후 수정 예정
  ) {
    FollowSummaryResult followSummaryResult = followService.summaryFollowInfo(
        targetUserId,
        logInUserId
    );
    return ResponseEntity.ok(followSummaryResult);
  }

  @GetMapping("/followings")
  public ResponseEntity<FollowListResponse<FollowResult>> getFollowings(
      @ModelAttribute SearchFollowingsCondition searchFollowingsCondition
  ) {
    FollowListResponse<FollowResult> followings = followService.getFollowings(
        searchFollowingsCondition);
    return ResponseEntity.ok(followings);
  }

  @GetMapping("/follower")
  public ResponseEntity<FollowListResponse<FollowResult>> getFollowers(
      @ModelAttribute SearchFollowersCondition followersCondition
  ) {
    FollowListResponse<FollowResult> followers = followService.getFollowers(followersCondition);
    return ResponseEntity.ok(followers);
  }

  @DeleteMapping("/{followId}")
  public ResponseEntity<Void> delete(@PathVariable("followId") Long followId) {
    followService.delete(followId);
    return ResponseEntity.noContent()
        .build();
  }

  @PatchMapping("/{followId}/soft-delete")
  public ResponseEntity<Void> softDelete(@PathVariable("followId") Long followId) {
    followService.softDelete(followId);
    return ResponseEntity.noContent()
        .build();
  }

}

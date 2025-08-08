package com.stylemycloset.follow.controller;

import static org.mockito.ArgumentMatchers.any;

import com.stylemycloset.follow.dto.FollowListResponse;
import com.stylemycloset.follow.dto.FollowResult;
import com.stylemycloset.follow.dto.FollowSummaryResult;
import com.stylemycloset.follow.dto.request.FollowCreateRequest;
import com.stylemycloset.follow.service.FollowService;
import com.stylemycloset.testutil.ControllerTestSupport;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

class FollowControllerTest extends ControllerTestSupport {

  @MockitoBean
  private FollowService followService;

  @Test
  @DisplayName("팔로우 생성: 필수값 누락이면 400")
  void startFollowing_missingFields_returns400() {
    // given
    FollowCreateRequest body = new FollowCreateRequest(null, 10L);

    // when
    MvcTestResult result = mvc.post()
        .uri("/api/follows")
        .content(convertToJsonRequest(body))
        .contentType(MediaType.APPLICATION_JSON)
        .exchange();

    // then
    Assertions.assertThat(result)
        .hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  @DisplayName("팔로우 생성: 정상 입력이면 200")
  void startFollowing_valid_returns200() {
    // given
    FollowCreateRequest body = new FollowCreateRequest(1L, 10L);
    BDDMockito.given(followService.startFollowing(any()))
        .willReturn(Mockito.mock(FollowResult.class));

    // when
    MvcTestResult result = mvc.post()
        .uri("/api/follows")
        .content(convertToJsonRequest(body))
        .contentType(MediaType.APPLICATION_JSON)
        .exchange();

    // then
    Assertions.assertThat(result)
        .hasStatus(HttpStatus.OK);
  }

  @Test
  @DisplayName("요약 조회: userId 누락이면 400")
  void summary_missingUserId_returns400() {
    // given & when
    MvcTestResult result = mvc.get()
        .uri("/api/follows/summary")
        .exchange();

    // then
    Assertions.assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  @DisplayName("요약 조회: userId 정상 전달이면 200")
  void summary_valid_returns200() {
    // given
    BDDMockito.given(followService.summaryFollow(any(), any()))
        .willReturn(Mockito.mock(FollowSummaryResult.class));

    // when
    MvcTestResult result = mvc.get()
        .uri("/api/follows/summary")
        .param("userId", String.valueOf(1L))
        .exchange();

    // then
    Assertions.assertThat(result).hasStatus(HttpStatus.OK);
  }

  @Test
  @DisplayName("팔로잉 조회: followerId 누락이면 400")
  void followings_missingFollowerId_returns400() {
    // given & then
    MvcTestResult result = mvc.get()
        .uri("/api/follows/followings?limit=10")
        .exchange();

    // then
    Assertions.assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  @DisplayName("팔로잉 조회: limit가 0 이하이면 400")
  void followings_limitInvalid_returns400() {
    // given & when
    MvcTestResult result = mvc.get()
        .uri("/api/follows/followings?followerId=1&limit=0")
        .exchange();

    // then
    Assertions.assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  @DisplayName("팔로잉 조회: 정상이면 200")
  void followings_valid_returns200() {
    // given
    BDDMockito.given(followService.getFollowings(any()))
        .willReturn(Mockito.mock(FollowListResponse.class));

    // when
    MvcTestResult result = mvc.get()
        .uri("/api/follows/followings?followerId=1&limit=10&sortDirection=DESC")
        .exchange();

    // then
    Assertions.assertThat(result).hasStatus(HttpStatus.OK);
  }

  @Test
  @DisplayName("팔로워 조회: followeeId 누락이면 400")
  void followers_missingFolloweeId_returns400() {
    // given & when
    MvcTestResult result = mvc.get()
        .uri("/api/follows/follower?limit=10")
        .exchange();

    // then
    Assertions.assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  @DisplayName("팔로워 조회: limit가 0 이하이면 400")
  void followers_limitInvalid_returns400() {
    // given & when
    MvcTestResult result = mvc.get()
        .uri("/api/follows/follower?followeeId=1&limit=0")
        .exchange();

    // then
    Assertions.assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  @DisplayName("팔로워 조회: 정상이면 200")
  void followers_valid_returns200() {
    // given
    BDDMockito.given(followService.getFollowers(any()))
        .willReturn(Mockito.mock(FollowListResponse.class));

    // when
    MvcTestResult result = mvc.get()
        .uri("/api/follows/follower?followeeId=1&limit=10&sortDirection=ASC")
        .exchange();

    // then
    Assertions.assertThat(result).hasStatus(HttpStatus.OK);
  }

  @Test
  @DisplayName("팔로우 소프트 삭제: 정상 요청 시 204 No Content를 반환한다")
  void softDelete_returns204() {
    // given & when
    MvcTestResult result = mvc.delete()
        .uri("/api/follows/{followId}", 1L)
        .exchange();

    // then
    Assertions.assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
  }

  @Test
  @DisplayName("팔로우 소프트 삭제: ID가 없을시 400")
  void softDelete_returns400() {
    // given & when
    MvcTestResult result = mvc.delete()
        .uri("/api/follows/")
        .exchange();

    // then
    Assertions.assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  @DisplayName("팔로우 하드 삭제: 정상 요청 시 204 No Content를 반환한다")
  void hardDelete_returns204() {
    // given & when
    MvcTestResult result = mvc.delete()
        .uri("/api/follows/{followId}/hard-delete", 1L)
        .exchange();

    // then
    Assertions.assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
  }

}
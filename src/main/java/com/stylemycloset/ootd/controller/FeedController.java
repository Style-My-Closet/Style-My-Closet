package com.stylemycloset.ootd.controller;

import com.stylemycloset.ootd.dto.FeedCreateRequest;
import com.stylemycloset.ootd.dto.FeedDto;
import com.stylemycloset.ootd.service.FeedService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedController {

  private final FeedService feedService;

  /**
   * 새로운 피드를 생성하고 생성된 피드 정보를 반환합니다.
   *
   * @param request 생성할 피드의 정보를 담은 요청 객체
   * @return 생성된 피드의 정보를 포함한 ResponseEntity (HTTP 201 Created)
   */
  @PostMapping
  public ResponseEntity<FeedDto> createFeed(@Valid @RequestBody FeedCreateRequest request) {
    // TODO: 실제로는 Spring Security를 통해 로그인한 사용자 ID를 가져와야 함
    // 지금은 request 에 있는 authorID를 그대로 사용
    FeedDto responseDto = feedService.createFeed(request);

    return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
  }
}

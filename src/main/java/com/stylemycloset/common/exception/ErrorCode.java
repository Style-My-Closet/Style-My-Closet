package com.stylemycloset.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

  ERROR_WEATHER_API("api 호출 오류", HttpStatus.INTERNAL_SERVER_ERROR),

  ERROR_S3_UPLOAD_INVALID_ARGUMENT("S3 업로드 요청에 유효하지 않은 매개변수가 들어왔습니다.", HttpStatus.NOT_FOUND),

  ERROR_FOLLOW_NOT_FOUND("팔로우 정보가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
  ERROR_BINARY_CONTENT_NOT_FOUND("해당 BinaryContent가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
  ERROR_FOLLOW_SELF_FORBIDDEN("자기자신을 팔로우할 수 없습니다.", HttpStatus.FORBIDDEN),
  ERROR_FOLLOW_ALREADY_EXIST("이미 팔로우한 유저입니다.", HttpStatus.BAD_REQUEST),
  ERROR_ACTIVE_FOLLOW_NOT_FOUND("활성화된 팔루우 정보자 존재하지 않습니다.", HttpStatus.BAD_REQUEST);

  private final String message;
  private final HttpStatus httpStatus;

  ErrorCode(String message, HttpStatus httpStatus) {
    this.message = message;
    this.httpStatus = httpStatus;
  }

}
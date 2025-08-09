package com.stylemycloset.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

  // Common
  INVALID_INPUT_VALUE("입력값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
  INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  ERROR_CODE("오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

  // User
  EMAIL_DUPLICATED("이메일이 중복 되었습니다.", HttpStatus.CONFLICT),
  USER_NOT_FOUND("유저의 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

  // Clothes
  CLOTHES_NOT_FOUND("일부 의상 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

  // Weather
  WEATHER_NOT_FOUND("날씨 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

  // Sse
  SSE_SEND_FAILURE("SSE 전송에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

  // S3
  ERROR_S3_UPLOAD_INVALID_ARGUMENT("S3 업로드 요청에 유효하지 않은 매개변수가 들어왔습니다.", HttpStatus.BAD_REQUEST),

  // Follow
  ERROR_FOLLOW_NOT_FOUND("팔로우 정보가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
  ERROR_BINARY_CONTENT_NOT_FOUND("해당 BinaryContent가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
  ERROR_FOLLOW_SELF_FORBIDDEN("자기 자신을 팔로우할 수 없습니다.", HttpStatus.FORBIDDEN),
  ERROR_FOLLOW_ALREADY_EXIST("이미 팔로우한 유저입니다.", HttpStatus.CONFLICT),
  ERROR_ACTIVE_FOLLOW_NOT_FOUND("활성화된 팔로우 정보가 존재하지 않습니다.", HttpStatus.NOT_FOUND);

  private final String message;
  private final HttpStatus httpStatus;

  ErrorCode(String message, HttpStatus httpStatus) {
    this.message = message;
    this.httpStatus = httpStatus;
  }

}
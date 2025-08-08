package com.stylemycloset.common.exception;

import lombok.Getter;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties.Http;
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
  WEATHER_NOT_FOUND("날씨 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

  private final String message;
  private final HttpStatus httpStatus;

  ErrorCode(String message, HttpStatus httpStatus) {
    this.message = message;
    this.httpStatus = httpStatus;
  }

}
package com.stylemycloset.common.exception;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

  ERROR_CODE("에러 메세지 예시", HttpStatus.OK),


  // User 에러
  EMAIL_DUPLICATED("이메일이 중복 되었습니다.", HttpStatus.CONFLICT),
  USER_NOT_FOUND("유저의 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  ;

  private final String message;
  private final HttpStatus httpStatus;

  ErrorCode(String message, HttpStatus httpStatus) {
    this.message = message;
    this.httpStatus = httpStatus;
  }

}
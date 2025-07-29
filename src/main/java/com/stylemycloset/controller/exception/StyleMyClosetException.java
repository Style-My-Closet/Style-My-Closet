package com.stylemycloset.controller.exception;

import java.time.Instant;
import java.util.Map;
import lombok.Getter;

@Getter
public class StyleMyClosetException extends RuntimeException {

  private final Instant timestamp;

  private final ErrorCode errorCode;

  private final Map<String, Object> details;

  public StyleMyClosetException(ErrorCode errorCode, Map<String, Object> details) {
    this.timestamp = Instant.now();
    this.errorCode = errorCode;
    this.details = details;
  }

}

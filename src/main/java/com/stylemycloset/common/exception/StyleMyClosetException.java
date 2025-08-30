package com.stylemycloset.common.exception;

import java.time.Instant;
import java.util.HashMap;
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

  public StyleMyClosetException(ErrorCode errorCode) {
    this.timestamp = Instant.now();
    this.errorCode = errorCode;
    this.details = new HashMap<>();
  }

  public StyleMyClosetException addDetails(String key, Object value) {
    this.details.put(key, value);
    return this;
  }

  public StyleMyClosetException addAllDetails(Map<String, Object> details) {
    this.details.putAll(details);
    return this;
  }

}

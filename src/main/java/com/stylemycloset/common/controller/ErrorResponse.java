package com.stylemycloset.common.controller;

import com.stylemycloset.common.controller.exception.StyleMyClosetException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;

public record ErrorResponse(
    Instant timestamp,
    String errorCodeName,
    String message,
    Map<String, Object> details,
    String exceptionType,
    int status
) {

  public static ErrorResponse of(StyleMyClosetException exception,
      int status) {
    return new ErrorResponse(
        exception.getTimestamp(),
        exception.getErrorCode().name(),
        exception.getErrorCode().getMessage(),
        exception.getDetails(),
        exception.getClass().getTypeName(),
        status
    );
  }

  public static ErrorResponse of(Exception exception, int status) {
    return new ErrorResponse(
        Instant.now(),
        exception.getClass().getSimpleName(),
        exception.getMessage(),
        null,
        exception.getClass().getTypeName(),
        status
    );
  }

  public static List<ErrorResponse> of(Exception exception, List<FieldError> fieldErrors) {
    return fieldErrors.stream()
        .map(fieldError -> new ErrorResponse(
            Instant.now(),
            exception.getClass().getSimpleName(),
            fieldError.getDefaultMessage(),
            Map.of("field", fieldError.getField()),
            fieldError.getClass().getTypeName(),
            HttpStatus.BAD_REQUEST.value())
        )
        .toList();
  }

}

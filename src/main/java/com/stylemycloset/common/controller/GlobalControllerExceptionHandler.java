package com.stylemycloset.common.controller;

import com.stylemycloset.common.exception.StyleMyClosetException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalControllerExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<List<ErrorResponse>> handleValidationErrors(
      MethodArgumentNotValidException exception) {
    List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
    log.error("Wrong Method Argument: {}", exception.getMessage());

    List<ErrorResponse> errorResponses = ErrorResponse.of(exception, fieldErrors);
    return ResponseEntity.badRequest()
        .body(errorResponses);
  }

  @ExceptionHandler(MissingServletRequestPartException.class)
  public ResponseEntity<ErrorResponse> handleMissingPart(
      MissingServletRequestPartException exception) {
    log.error("Missing multipart part: {}", exception.getRequestPartName());

    ErrorResponse errorResponse = ErrorResponse.of(exception, HttpStatus.BAD_REQUEST.value());
    return ResponseEntity.badRequest()
        .body(errorResponse);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<Object> handleNoResourceFound(NoResourceFoundException exception) {
    log.error("No static Resource: {}", exception.getMessage());

    ErrorResponse errorResponse = ErrorResponse.of(exception, HttpStatus.BAD_REQUEST.value());
    return ResponseEntity.badRequest()
        .body(errorResponse);
  }

  @ExceptionHandler(StyleMyClosetException.class)
  public ResponseEntity<ErrorResponse> handleStyleMyClosetException(
      StyleMyClosetException exception) {
    log.error("커스텀 예외 발생: errorCodeName={}, message={}", exception.getErrorCode(),
        exception.getMessage(),
        exception);
    HttpStatus status = exception.getErrorCode().getHttpStatus();
    ErrorResponse errorResponse = ErrorResponse.of(exception, status.value());
    return ResponseEntity.status(status)
        .body(errorResponse);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception exception) {
    log.error("Not SpecificException: {}", exception.getMessage());

    ErrorResponse errorResponse = ErrorResponse.of(exception,
        HttpStatus.INTERNAL_SERVER_ERROR.value());
    return ResponseEntity.internalServerError()
        .body(errorResponse);
  }

}

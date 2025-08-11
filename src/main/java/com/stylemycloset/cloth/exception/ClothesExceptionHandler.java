package com.stylemycloset.cloth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ClothesExceptionHandler {

    @ExceptionHandler(ClothesException.class)
    public ResponseEntity<ErrorResponse> handleClothesException(ClothesException e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .exceptionName(e.getExceptionName().name())
                .message(e.getMessage())
                .details(e.getDetails())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        java.util.Map<String, Object> details = new java.util.HashMap<>();
        details.put("error", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .exceptionName("INTERNAL_ERROR")
                .message("옷 목록 조회에 실패했습니다.")
                .details(details)
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }
}

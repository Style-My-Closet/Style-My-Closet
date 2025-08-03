package com.stylemycloset.cloth.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
@Getter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class ErrorResponse {
    private String exceptionName;
    private String message;
    private Map<String, String> details;


}

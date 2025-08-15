package com.stylemycloset.cloth.exception;

import com.stylemycloset.common.exception.ErrorCode;
import lombok.Getter;

import java.util.Map;

@Getter
public class ClothesException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Map<String, Object> details;
    
    public ClothesException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = null;
    }
    
    public ClothesException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = details;
    }
}

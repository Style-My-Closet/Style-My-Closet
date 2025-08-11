package com.stylemycloset.cloth.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class ClothesException extends RuntimeException {
    private final ClothingErrorCode exceptionName;
    private final Map<String, Object> details;
    

    
    public ClothesException(ClothingErrorCode exceptionName) {
        super(exceptionName.getMessage());
        this.exceptionName = exceptionName;
        this.details = null;
    }
    

}

package com.stylemycloset.cloth.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class ClothesException extends RuntimeException {
    private final ClothingErrorCode exceptionName;
    private final Map<String, String> details;
    
    public ClothesException(ClothingErrorCode exceptionName, Map<String, String> details) {
        super(exceptionName.getMessage());
        this.exceptionName = exceptionName;
        this.details = details;
    }
    
    public ClothesException(ClothingErrorCode exceptionName) {
        super(exceptionName.getMessage());
        this.exceptionName = exceptionName;
        this.details = null;
    }
    

}

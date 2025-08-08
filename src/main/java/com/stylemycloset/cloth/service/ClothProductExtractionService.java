package com.stylemycloset.cloth.service;

import com.stylemycloset.cloth.dto.response.ClothExtractionResponseDto;
import com.stylemycloset.cloth.dto.ClothResponseDto;

public interface ClothProductExtractionService {
    
    ClothExtractionResponseDto extractProductInfoFromUrl(String productUrl);

    // 컨트롤러 응답 DTO로 가공까지 포함한 서비스 메서드
    ClothResponseDto buildClothResponse(String productUrl);
    
    boolean isValidProductUrl(String productUrl);
} 
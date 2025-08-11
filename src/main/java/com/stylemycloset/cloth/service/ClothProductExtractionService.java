package com.stylemycloset.cloth.service;

import com.stylemycloset.cloth.dto.ClothResponseDto;

public interface ClothProductExtractionService {
    ClothResponseDto extractAndSave(String productUrl, Long userId);
}
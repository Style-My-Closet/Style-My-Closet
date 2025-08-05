package com.stylemycloset.cloth.dto;

import com.stylemycloset.cloth.entity.ClothingCategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;


public record ClothesUpdateRequest(
    @NotBlank(message = "의류 이름은 필수입니다.")
    @Size(min = 1, max = 100, message = "의류 이름은 1자 이상 100자 이하여야 합니다.")
    String name,                                    // 의상 이름
    
    @NotNull(message = "의류 타입은 필수입니다.")
    ClothingCategoryType type,                      // 의상 타입
    
    List<ClothesAttributeDto> attributes     // 의상 속성
) {
} 
package com.stylemycloset.cloth.dto;

import com.stylemycloset.cloth.entity.ClothingAttributeValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ClothesAttributeDto(
    @NotNull(message = "속성 정의 ID는 필수입니다.")
    Long definitionId,  // 속성 정의 ID
    
    String definitionName,  // 속성 정의 이름
    
    List<String> selectableValues,  // 선택 가능한 값들
    
    @NotBlank(message = "속성 값은 필수입니다.")
    String value        // 속성 값
) {

    public static ClothesAttributeDto from(ClothingAttributeValue clothingAttributeValue) {
        return new ClothesAttributeDto(
            clothingAttributeValue.getAttribute().getId(),
            clothingAttributeValue.getAttribute().getName(),
            clothingAttributeValue.getAttribute().getOptions().stream()
                    .map(option -> option.getValue())
                    .toList(),
            clothingAttributeValue.getOption().getValue()
        );
    }
} 
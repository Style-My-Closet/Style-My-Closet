package com.stylemycloset.cloth.dto;

import com.querydsl.core.annotations.QueryProjection;
import com.stylemycloset.cloth.entity.ClothingAttributeValue;

import java.util.List;

public record AttributeDto(
    Long definitionId,  
    String definitionName,  
    List<String> selectableValues,  // 선택 가능한 값들 추가
    String value
) {
    @QueryProjection
    public AttributeDto(Long definitionId, String definitionName, String value) {
        this(
            definitionId,
            definitionName,
            List.of(),
            value
        );
    }

    public static AttributeDto from(ClothingAttributeValue clothingAttributeValue) {
        return new AttributeDto(
            clothingAttributeValue.getAttribute().getId(),
            clothingAttributeValue.getAttribute().getName(),
            clothingAttributeValue.getAttribute().getOptions().stream()  // 선택 가능한 값들 추출
                    .map(option -> option.getValue())
                    .toList(),
            clothingAttributeValue.getOption().getValue()
        );
    }
} 
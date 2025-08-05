package com.stylemycloset.cloth.dto;

import com.stylemycloset.cloth.entity.ClothingAttribute;
import com.stylemycloset.cloth.entity.AttributeOption;

import java.util.List;

//반환
public record ClothesAttributeDefDto(
    Long id,                    // 속성 정의 ID
    String name,                // 속성 정의 이름
    List<String> selectableValues // 선택 가능한 값 목록
) {

    public static ClothesAttributeDefDto from(ClothingAttribute attribute) {
        if (attribute == null) {
            throw new IllegalArgumentException("ClothingAttribute must not be null");
        }
        List<String> selectableValues = attribute.getActiveOptions()
                .stream()
                .map(AttributeOption::getValue)
                .toList();
        
        return new ClothesAttributeDefDto(
            attribute.getId(),
            attribute.getName(),
            selectableValues
        );
    }
} 
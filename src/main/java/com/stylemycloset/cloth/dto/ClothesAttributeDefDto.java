package com.stylemycloset.cloth.dto;

import com.stylemycloset.cloth.entity.ClothingAttribute;
import com.stylemycloset.cloth.entity.AttributeOption;

import java.time.Instant;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

//반환
public record ClothesAttributeDefDto(
    Long id,                    // 속성 정의 ID
    String name,                // 속성 정의 이름
    @JsonProperty("created_at") Instant createdAt,          // 생성일
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
            attribute.getCreatedAt(),
            selectableValues
        );
    }

    // FE 호환을 위해 camelCase 키도 함께 노출
    @JsonProperty("createdAt")
    public Instant createdAtCamel() {
        return createdAt;
    }
} 
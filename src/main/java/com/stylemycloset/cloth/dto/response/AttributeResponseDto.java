package com.stylemycloset.cloth.dto.response;

import com.stylemycloset.cloth.entity.AttributeOption;
import com.stylemycloset.cloth.entity.ClothingAttribute;
import com.stylemycloset.cloth.entity.ClothingAttributeValue;

import java.util.List;

public record AttributeResponseDto(
        String id,
        String name,
        List<String> selectableValues
) {
    public static AttributeResponseDto from(ClothingAttribute attribute) {
        List<String> values = attribute.getActiveOptions().stream()
                .map(AttributeOption::getValue)
                .toList();

        return new AttributeResponseDto(
                attribute.getId().toString(),
                attribute.getName(),
                values
        );
    }
}



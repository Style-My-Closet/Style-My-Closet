package com.stylemycloset.cloth.dto.response;

import com.stylemycloset.cloth.entity.AttributeOption;
import com.stylemycloset.cloth.entity.ClothingAttribute;

import java.util.List;

public record AttributeCreateResponseDto
        (
                String id,
                String name,
                List<String> selectableValues
        ){
    public static AttributeCreateResponseDto from(ClothingAttribute attribute) {
        List<String> values = attribute.getActiveOptions().stream()
                .map(AttributeOption::getValue)
                .toList();

        return new AttributeCreateResponseDto(
                attribute.getId().toString(),
                attribute.getName(),
                values
        );
    }
}

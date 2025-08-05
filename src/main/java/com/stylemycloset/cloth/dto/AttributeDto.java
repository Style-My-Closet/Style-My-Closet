package com.stylemycloset.cloth.dto;

import com.stylemycloset.cloth.entity.ClothingAttributeValue;

public record AttributeDto(
    Long attributeId,
    String attributeName,
    Long optionId,
    String optionValue
) {
    public static AttributeDto from(ClothingAttributeValue clothingAttributeValue) {
        return new AttributeDto(
            clothingAttributeValue.getAttribute().getId(),
            clothingAttributeValue.getAttribute().getName(),
            clothingAttributeValue.getOption().getId(),
            clothingAttributeValue.getOption().getValue()
        );
    }
} 
package com.stylemycloset.cloth.dto;

import com.stylemycloset.cloth.entity.AttributeOption;
import com.stylemycloset.cloth.entity.ClothingAttribute;
import com.stylemycloset.cloth.entity.ClothingAttributeValue;

import java.util.List;


public record ClothesAttributeWithDefDto(
    Long definitionId,
    String definitionName,
    List<String> selectableValues, // 선택 가능한 값 목록
    String value
) {
   
    public static ClothesAttributeWithDefDto from(ClothingAttributeValue clothingAttributeValue) {
        ClothingAttribute attribute = clothingAttributeValue.getAttribute();
        Long defId = attribute.getId();
        String defName = attribute.getName();

        List<String> selectList = attribute.getOptions()
                .stream()
                .map(AttributeOption::getValue)
                .toList();

        String val = clothingAttributeValue.getOption().getValue();
        return new ClothesAttributeWithDefDto(defId, defName, selectList, val);
    }

    public static ClothesAttributeWithDefDto from(Long definitionId, String definitionName, List<String> selectableValues, String value) {
        return new ClothesAttributeWithDefDto(definitionId, definitionName, selectableValues, value);
    }
} 
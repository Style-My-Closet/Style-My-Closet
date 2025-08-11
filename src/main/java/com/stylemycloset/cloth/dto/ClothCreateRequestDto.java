package com.stylemycloset.cloth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ClothCreateRequestDto {
    private String name;
    private String type;
    private Long categoryId;
    private UUID binaryContentId;
    private List<AttributeRequestDto> attributes;
    
    @Getter
    @Setter
    @NoArgsConstructor
    public static class AttributeRequestDto {
        private Long definitionId;
        private Long optionId;

    }
}
package com.stylemycloset.cloth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClothUpdateRequestDto {
    @NotNull
    private String name;
    @NotNull
    private String type;
    private List<AttributeRequestDto> attributes;
    
    @Getter
    @Setter
    @NoArgsConstructor
    public static class AttributeRequestDto {
        private Long definitionId;
        private Long optionId;
    }
} 
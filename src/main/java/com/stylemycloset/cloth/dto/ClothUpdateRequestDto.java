package com.stylemycloset.cloth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClothUpdateRequestDto {
    private String name;
    private String description;
    private Long categoryId;
    private List<String> attributes;
} 
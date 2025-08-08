package com.stylemycloset.cloth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ClothImageExtractionRequestDto {
    
    @NotBlank(message = "이미지 URL은 필수입니다.")
    @Pattern(regexp = "^(https?://).*", message = "올바른 URL 형식이 아닙니다.")
    private String imageUrl;
    
    public ClothImageExtractionRequestDto(String imageUrl) {
        this.imageUrl = imageUrl;
    }
} 
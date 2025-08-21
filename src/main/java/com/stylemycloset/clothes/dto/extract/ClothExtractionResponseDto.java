package com.stylemycloset.clothes.dto.extract;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class ClothExtractionResponseDto {
    
    private String productName;             // 상품명
    private String brand;                   // 브랜드
    private String category;                // 카테고리
    private List<String> colors;            // 색상 옵션들
    private List<String> sizes;             // 사이즈 옵션들
    private String material;                // 소재
    private BigDecimal price;               // 가격
    private String originalPrice;           // 원가 (할인 전)
    private String discountRate;            // 할인율
    private List<String> images;            // 상품 이미지들
    private String productUrl;              // 원본 상품 URL
    private String description;             // 상품 설명
    private Boolean isAvailable;            // 재고 여부
    
    // 추출 실패 시 사용
    public static ClothExtractionResponseDto createFailureResponse(String productUrl) {
        return ClothExtractionResponseDto.builder()
                .productName("추출 실패")
                .brand("알 수 없음")
                .category("알 수 없음")
                .colors(List.of())
                .sizes(List.of())
                .material("알 수 없음")
                .price(BigDecimal.ZERO)
                .originalPrice("0")
                .discountRate("0%")
                .images(List.of())
                .productUrl(productUrl)
                .description("상품 정보를 가져올 수 없습니다.")
                .isAvailable(false)
                .build();
    }
} 
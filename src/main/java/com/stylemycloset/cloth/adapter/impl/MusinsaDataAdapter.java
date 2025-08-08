package com.stylemycloset.cloth.adapter.impl;

import com.stylemycloset.cloth.adapter.AbstractSiteDataAdapter;
import com.stylemycloset.cloth.dto.RawSiteData;
import com.stylemycloset.cloth.dto.response.ClothExtractionResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
public class MusinsaDataAdapter extends AbstractSiteDataAdapter {
    
    @Override
    public boolean supports(String url) {
        return url != null && url.contains("musinsa.com");
    }
    
    @Override
    public String getSiteName() {
        return "무신사";
    }
    
    @Override
    public int getPriority() {
        return 10; // 높은 우선순위
    }
    
    @Override
    public ClothExtractionResponseDto convert(RawSiteData rawData) {
        log.info("무신사 데이터 변환 시작: {}", rawData.getSourceUrl());
        
        // 상품명 처리 (메타태그에서 가져온 경우 브랜드명과 불필요한 텍스트 제거)
        String productName = extractProductName(rawData);
        String brand = rawData.getFieldOrDefault("brand", "브랜드 정보 없음");
        
        // 가격 처리
        BigDecimal price = parsePrice(rawData.getField("price"));
        
        // 카테고리 추출
        String category = extractCategoryFromUrl(rawData.getSourceUrl());
        
        // 이미지 처리
        List<String> images = rawData.getExtractedImages().isEmpty() ? 
                              getDefaultImages() : rawData.getExtractedImages();
        
        return ClothExtractionResponseDto.builder()
                .productName(productName)
                .brand(brand)
                .category(category)
                .colors(getDefaultColors())
                .sizes(getDefaultSizes(category))
                .material(rawData.getFieldOrDefault("material", "소재 정보 없음"))
                .price(price)
                .originalPrice(rawData.getField("originalPrice"))
                .discountRate(rawData.getField("discountRate"))
                .images(images)
                .productUrl(rawData.getSourceUrl())
                .description(getSiteName() + "에서 추출한 상품 정보입니다.")
                .isAvailable(true)
                .build();
    }
    
    private String extractProductName(RawSiteData rawData) {
        String productName = rawData.getField("productName");
        String brand = rawData.getField("brand");
        
        if (productName == null) {
            return "상품명을 가져올 수 없습니다";
        }
        
        // 무신사 특화 텍스트 정리
        productName = cleanText(productName);
        
        // 브랜드명 제거
        if (brand != null) {
            productName = removeBrand(productName, brand);
        }
        
        // 무신사 특화 불필요한 텍스트 제거
        productName = productName
                .replace("- 사이즈 & 후기 | 무신사", "")
                .replace("| 무신사", "")
                .replaceAll("^[-\\s]+", "")
                .replaceAll("[-\\s]+$", "")
                .trim();
        
        return productName.isEmpty() ? "상품명을 가져올 수 없습니다" : productName;
    }
}
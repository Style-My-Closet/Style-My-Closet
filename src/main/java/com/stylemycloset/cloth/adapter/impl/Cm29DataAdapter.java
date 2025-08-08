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
public class Cm29DataAdapter extends AbstractSiteDataAdapter {
    
    @Override
    public boolean supports(String url) {
        return url != null && url.contains("29cm.co.kr");
    }
    
    @Override
    public String getSiteName() {
        return "29CM";
    }
    
    @Override
    public int getPriority() {
        return 20;
    }
    
    @Override
    public ClothExtractionResponseDto convert(RawSiteData rawData) {
        log.info("29cm 데이터 변환 시작: {}", rawData.getSourceUrl());
        
        // 상품명 처리
        String productName = extractProductName(rawData);
        String brand = rawData.getFieldOrDefault("brand", "브랜드 정보 없음");
        
        // 가격 처리 (29cm은 콤마가 포함된 형태일 수 있음)
        BigDecimal price = parsePrice(rawData.getField("price"));
        
        // 카테고리 추출 (신발 URL 특별 처리)
        String category = extractCategoryFromUrl(rawData.getSourceUrl());
        if (category.equals("기타") && rawData.getSourceUrl().contains("shoes")) {
            category = "신발";
        }
        
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
            return getSiteName() + " 상품";
        }
        
        // 29cm 특화 텍스트 정리
        productName = cleanText(productName);
        
        // 브랜드명 제거
        if (brand != null) {
            productName = removeBrand(productName, brand);
        }
        
        return productName.isEmpty() ? getSiteName() + " 상품" : productName;
    }
}
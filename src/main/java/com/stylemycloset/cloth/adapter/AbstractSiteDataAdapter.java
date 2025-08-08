package com.stylemycloset.cloth.adapter;

import com.stylemycloset.cloth.dto.RawSiteData;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public abstract class AbstractSiteDataAdapter implements SiteDataAdapter {
    
    private static final Pattern PRICE_PATTERN = Pattern.compile("[^0-9]");
    
    /**
     * 가격 텍스트를 BigDecimal로 변환
     */
    protected BigDecimal parsePrice(String priceText) {
        if (priceText == null || priceText.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        try {
            // 숫자가 아닌 모든 문자 제거
            String cleanPrice = PRICE_PATTERN.matcher(priceText).replaceAll("");
            return cleanPrice.isEmpty() ? BigDecimal.ZERO : new BigDecimal(cleanPrice);
        } catch (NumberFormatException e) {
            log.warn("가격 파싱 실패: {}", priceText, e);
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * 텍스트 정리 (앞뒤 공백, 특수문자 제거)
     */
    protected String cleanText(String text) {
        if (text == null) {
            return null;
        }
        return text.trim()
                   .replaceAll("\\s+", " ")  // 여러 공백을 하나로
                   .replaceAll("^\"|\"$", ""); // 앞뒤 따옴표 제거
    }
    
    /**
     * 상품명에서 브랜드명 제거
     */
    protected String removeBrand(String productName, String brand) {
        if (productName == null || brand == null) {
            return productName;
        }
        
        return productName
                .replace(brand, "")
                .replace("(" + brand + ")", "")
                .replace("[" + brand + "]", "")
                .replaceAll("^[-\\s]+", "")  // 앞의 - 나 공백 제거
                .replaceAll("[-\\s]+$", "")  // 뒤의 - 나 공백 제거
                .trim();
    }
    
    /**
     * URL에서 카테고리 추출
     */
    protected String extractCategoryFromUrl(String url) {
        if (url == null) return "ACCESSORY";
        
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.contains("/outer/") || lowerUrl.contains("outer")) return "OUTER";
        if (lowerUrl.contains("/top/") || lowerUrl.contains("top")) return "TOP";
        if (lowerUrl.contains("/bottom/") || lowerUrl.contains("bottom")) return "BOTTOM";
        if (lowerUrl.contains("/dress/") || lowerUrl.contains("dress")) return "DRESS";
        if (lowerUrl.contains("/shoes/") || lowerUrl.contains("shoes")) return "SHOES";
        if (lowerUrl.contains("/bag/") || lowerUrl.contains("bag") || lowerUrl.contains("backpack")) return "ACCESSORY";
        if (lowerUrl.contains("/accessory/") || lowerUrl.contains("accessory")) return "ACCESSORY";
        
        return "ACCESSORY";  // 기본값을 ACCESSORY로 변경
    }
    
    /**
     * 기본 이미지 반환
     */
    protected List<String> getDefaultImages() {
        // 실제 다운로드 가능한 이미지 URL 사용
        return List.of("https://httpbin.org/image/jpeg");
    }
    
    /**
     * 기본 색상 반환
     */
    protected List<String> getDefaultColors() {
        return List.of("기본색상");
    }
    
    /**
     * 기본 사이즈 반환 (카테고리별)
     */
    protected List<String> getDefaultSizes(String category) {
        if ("신발".equals(category)) {
            return List.of("230", "235", "240", "245", "250", "255", "260", "265", "270", "275", "280");
        } else {
            return List.of("XS", "S", "M", "L", "XL", "XXL");
        }
    }
}
package com.stylemycloset.cloth.parser;

import com.stylemycloset.cloth.dto.response.ClothExtractionResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Component
public class DomainMapper {
    
    private static final Pattern PRICE_PATTERN = Pattern.compile("[^0-9]");
    private static final Map<String, String> CATEGORY_MAPPINGS = initializeCategoryMappings();
    private static final Map<String, List<String>> SIZE_MAPPINGS = initializeSizeMappings();
    
    /**
     * 4차: 추출된 모든 데이터를 공통 도메인 모델로 변환
     */
    public ClothExtractionResponseDto mapToDomainModel(
            Map<String, Object> metadata,      // 1차: 메타데이터
            Map<String, Object> contentData,   // 2차: 컨텐츠 데이터  
            Map<String, String> ruleData,      // 3차: 규칙 기반 데이터
            String sourceUrl) {
        
        log.info("도메인 모델 매핑 시작 - 메타데이터: {}, 컨텐츠: {}, 규칙: {}", 
                metadata.size(), contentData.size(), ruleData.size());
        
        // 우선순위: 1차 > 2차 > 3차 순으로 데이터 병합
        Map<String, Object> mergedData = mergeDataWithPriority(metadata, contentData, ruleData);
        
        return ClothExtractionResponseDto.builder()
                .productName(extractProductName(mergedData))
                .brand(extractBrand(mergedData))
                .category(extractCategory(mergedData, sourceUrl))
                .colors(extractColors(mergedData))
                .sizes(extractSizes(mergedData))
                .material(extractMaterial(mergedData))
                .price(extractPrice(mergedData))
                .originalPrice(extractOriginalPrice(mergedData))
                .discountRate(extractDiscountRate(mergedData))
                .images(extractImages(mergedData))
                .productUrl(sourceUrl)
                .description(generateDescription(mergedData, sourceUrl))
                .isAvailable(extractAvailability(mergedData))
                .build();
    }
    
    private Map<String, Object> mergeDataWithPriority(
            Map<String, Object> metadata,
            Map<String, Object> contentData,
            Map<String, String> ruleData) {
        
        Map<String, Object> merged = new HashMap<>();
        
        // 3차 (가장 낮은 우선순위)
        merged.putAll(ruleData);
        
        // 2차 (중간 우선순위)
        merged.putAll(contentData);
        
        // 1차 (가장 높은 우선순위) - 메타데이터 펼치기
        if (metadata.containsKey("jsonld")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> jsonLd = (Map<String, Object>) metadata.get("jsonld");
            merged.putAll(jsonLd);
        }
        
        if (metadata.containsKey("opengraph")) {
            @SuppressWarnings("unchecked")
            Map<String, String> og = (Map<String, String>) metadata.get("opengraph");
            merged.putAll(og);
        }
        
        if (metadata.containsKey("twitter")) {
            @SuppressWarnings("unchecked")
            Map<String, String> twitter = (Map<String, String>) metadata.get("twitter");
            merged.putAll(twitter);
        }
        
        return merged;
    }
    
    private String extractProductName(Map<String, Object> data) {
        // 다양한 키에서 상품명 추출 시도
        String[] nameKeys = {
            "name",           // JSON-LD
            "og:title",       // OpenGraph
            "twitter:title",  // Twitter Card
            "productName"     // 규칙 기반
        };
        
        for (String key : nameKeys) {
            Object value = data.get(key);
            if (value != null) {
                String name = cleanProductName(value.toString());
                if (!name.isEmpty()) {
                    return name;
                }
            }
        }
        
        return "상품명 없음";
    }
    
    private String extractBrand(Map<String, Object> data) {
        String[] brandKeys = {
            "brand",          // JSON-LD
            "product:brand",  // OpenGraph
            "brand"           // 규칙 기반
        };
        
        for (String key : brandKeys) {
            Object value = data.get(key);
            if (value != null && !value.toString().trim().isEmpty()) {
                return cleanText(value.toString());
            }
        }
        
        return "브랜드 정보 없음";
    }
    
    private String extractCategory(Map<String, Object> data, String url) {
        // 데이터에서 카테고리 추출
        String[] categoryKeys = {
            "category",       // JSON-LD
            "product:category", // OpenGraph
            "category"        // 규칙 기반
        };
        
        for (String key : categoryKeys) {
            Object value = data.get(key);
            if (value != null) {
                String category = normalizeCategory(value.toString());
                if (category != null) {
                    return category;
                }
            }
        }
        
        // URL에서 카테고리 추출
        return extractCategoryFromUrl(url);
    }
    
    private BigDecimal extractPrice(Map<String, Object> data) {
        String[] priceKeys = {
            "price",                    // JSON-LD
            "offers.price",             // JSON-LD nested
            "product:price:amount",     // OpenGraph
            "price"                     // 규칙 기반
        };
        
        for (String key : priceKeys) {
            Object value = data.get(key);
            if (value != null) {
                BigDecimal price = parsePrice(value.toString());
                if (price.compareTo(BigDecimal.ZERO) > 0) {
                    return price;
                }
            }
        }
        
        return BigDecimal.ZERO;
    }
    
    private String extractOriginalPrice(Map<String, Object> data) {
        String[] originalPriceKeys = {
            "originalPrice",
            "product:price:normal_price",
            "highPrice"
        };
        
        for (String key : originalPriceKeys) {
            Object value = data.get(key);
            if (value != null && !value.toString().trim().isEmpty()) {
                return value.toString();
            }
        }
        
        return null;
    }
    
    private String extractDiscountRate(Map<String, Object> data) {
        String[] discountKeys = {
            "discountRate",
            "product:price:sale_rate",
            "discountPercentage"
        };
        
        for (String key : discountKeys) {
            Object value = data.get(key);
            if (value != null && !value.toString().trim().isEmpty()) {
                return normalizeDiscountRate(value.toString());
            }
        }
        
        return null;
    }
    
    private List<String> extractColors(Map<String, Object> data) {
        Object colors = data.get("color");
        if (colors != null) {
            if (colors instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> colorList = (List<String>) colors;
                return colorList;
            } else {
                return List.of(colors.toString());
            }
        }
        
        return List.of("기본색상");
    }
    
    private List<String> extractSizes(Map<String, Object> data) {
        Object sizes = data.get("size");
        if (sizes != null) {
            if (sizes instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> sizeList = (List<String>) sizes;
                return sizeList;
            } else {
                return List.of(sizes.toString());
            }
        }
        
        // 카테고리별 기본 사이즈
        String category = (String) data.get("category");
        return getDefaultSizesForCategory(category);
    }
    
    private String extractMaterial(Map<String, Object> data) {
        String[] materialKeys = {
            "material",
            "fabric",
            "composition"
        };
        
        for (String key : materialKeys) {
            Object value = data.get(key);
            if (value != null && !value.toString().trim().isEmpty()) {
                return value.toString();
            }
        }
        
        return "소재 정보 없음";
    }
    
    private List<String> extractImages(Map<String, Object> data) {
        String[] imageKeys = {
            "image",          // JSON-LD
            "og:image",       // OpenGraph
            "twitter:image",  // Twitter Card
            "images"          // 규칙 기반
        };
        
        for (String key : imageKeys) {
            Object value = data.get(key);
            if (value != null) {
                if (value instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> imageList = (List<String>) value;
                    return imageList;
                } else {
                    return List.of(value.toString());
                }
            }
        }
        
        return List.of("https://via.placeholder.com/400x500?text=이미지+없음");
    }
    
    private Boolean extractAvailability(Map<String, Object> data) {
        Object availability = data.get("availability");
        if (availability != null) {
            String avail = availability.toString().toLowerCase();
            return avail.contains("instock") || avail.contains("available");
        }
        
        return true; // 기본값: 구매 가능
    }
    
    private String generateDescription(Map<String, Object> data, String url) {
        Object source = data.get("source");
        String sourceName = source != null ? source.toString() : "크롤링";
        return String.format("%s에서 추출한 상품 정보입니다.", sourceName);
    }
    
    // 유틸리티 메소드들
    private String cleanProductName(String name) {
        if (name == null) return "";
        
        return name.trim()
                  .replaceAll("\\s*-\\s*사이즈.*", "")
                  .replaceAll("\\s*\\|\\s*.*", "")
                  .replaceAll("\\s+", " ")
                  .trim();
    }
    
    private String cleanText(String text) {
        if (text == null) return "";
        return text.trim().replaceAll("\\s+", " ");
    }
    
    private BigDecimal parsePrice(String priceText) {
        if (priceText == null) return BigDecimal.ZERO;
        
        try {
            String cleanPrice = PRICE_PATTERN.matcher(priceText).replaceAll("");
            return cleanPrice.isEmpty() ? BigDecimal.ZERO : new BigDecimal(cleanPrice);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
    
    private String normalizeCategory(String category) {
        if (category == null) return null;
        return CATEGORY_MAPPINGS.getOrDefault(category.toLowerCase(), category);
    }
    
    private String extractCategoryFromUrl(String url) {
        if (url == null) return "기타";
        
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.contains("shoes") || lowerUrl.contains("신발")) return "신발";
        if (lowerUrl.contains("outer") || lowerUrl.contains("아우터")) return "아우터";
        if (lowerUrl.contains("top") || lowerUrl.contains("상의")) return "상의";
        if (lowerUrl.contains("bottom") || lowerUrl.contains("하의")) return "하의";
        if (lowerUrl.contains("dress") || lowerUrl.contains("원피스")) return "원피스";
        if (lowerUrl.contains("bag") || lowerUrl.contains("가방")) return "가방";
        
        return "기타";
    }
    
    private String normalizeDiscountRate(String discountRate) {
        if (discountRate == null) return null;
        
        String rate = discountRate.replaceAll("[^0-9.]", "");
        if (!rate.isEmpty()) {
            return rate + "%";
        }
        
        return discountRate;
    }
    
    private List<String> getDefaultSizesForCategory(String category) {
        return SIZE_MAPPINGS.getOrDefault(category, List.of("FREE"));
    }
    
    private static Map<String, String> initializeCategoryMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("shoes", "신발");
        mappings.put("sneakers", "신발");
        mappings.put("boots", "신발");
        mappings.put("outer", "아우터");
        mappings.put("jacket", "아우터");
        mappings.put("coat", "아우터");
        mappings.put("top", "상의");
        mappings.put("shirt", "상의");
        mappings.put("tshirt", "상의");
        mappings.put("bottom", "하의");
        mappings.put("pants", "하의");
        mappings.put("jeans", "하의");
        return mappings;
    }
    
    private static Map<String, List<String>> initializeSizeMappings() {
        Map<String, List<String>> mappings = new HashMap<>();
        mappings.put("신발", List.of("230", "235", "240", "245", "250", "255", "260", "265", "270", "275", "280"));
        mappings.put("상의", List.of("XS", "S", "M", "L", "XL", "XXL"));
        mappings.put("하의", List.of("XS", "S", "M", "L", "XL", "XXL"));
        mappings.put("아우터", List.of("XS", "S", "M", "L", "XL", "XXL"));
        mappings.put("원피스", List.of("XS", "S", "M", "L", "XL", "XXL"));
        return mappings;
    }
}
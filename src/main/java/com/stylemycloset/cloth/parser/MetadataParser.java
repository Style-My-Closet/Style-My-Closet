package com.stylemycloset.cloth.parser;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class MetadataParser {
    
    /**
     * 1차: JSON-LD에서 구조화된 데이터 추출
     */
    public Optional<Map<String, Object>> parseJsonLd(WebDriver driver) {
        try {
            List<WebElement> jsonLdElements = driver.findElements(
                By.cssSelector("script[type='application/ld+json']"));
            
            for (WebElement element : jsonLdElements) {
                String jsonContent = element.getAttribute("innerHTML");
                if (jsonContent != null && !jsonContent.trim().isEmpty()) {
                    // JSON 파싱 로직 (간단한 예시)
                    Map<String, Object> jsonLdData = parseJsonLdContent(jsonContent);
                    if (isProductData(jsonLdData)) {
                        log.info("JSON-LD 상품 데이터 발견: {}", jsonLdData.get("@type"));
                        return Optional.of(jsonLdData);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("JSON-LD 파싱 실패", e);
        }
        return Optional.empty();
    }
    
    /**
     * OpenGraph 메타데이터 추출
     */
    public Map<String, String> parseOpenGraph(WebDriver driver) {
        Map<String, String> ogData = new HashMap<>();
        
        try {
            List<WebElement> ogElements = driver.findElements(
                By.cssSelector("meta[property^='og:']"));
            
            for (WebElement element : ogElements) {
                String property = element.getAttribute("property");
                String content = element.getAttribute("content");
                if (property != null && content != null) {
                    ogData.put(property, content);
                }
            }
            
            // Product 관련 OpenGraph 데이터도 추출
            List<WebElement> productElements = driver.findElements(
                By.cssSelector("meta[property^='product:']"));
            
            for (WebElement element : productElements) {
                String property = element.getAttribute("property");
                String content = element.getAttribute("content");
                if (property != null && content != null) {
                    ogData.put(property, content);
                }
            }
            
            log.info("OpenGraph 데이터 {}개 추출", ogData.size());
        } catch (Exception e) {
            log.warn("OpenGraph 파싱 실패", e);
        }
        
        return ogData;
    }
    
    /**
     * Twitter Card 메타데이터 추출
     */
    public Map<String, String> parseTwitterCard(WebDriver driver) {
        Map<String, String> twitterData = new HashMap<>();
        
        try {
            List<WebElement> twitterElements = driver.findElements(
                By.cssSelector("meta[name^='twitter:']"));
            
            for (WebElement element : twitterElements) {
                String name = element.getAttribute("name");
                String content = element.getAttribute("content");
                if (name != null && content != null) {
                    twitterData.put(name, content);
                }
            }
            
            log.info("Twitter Card 데이터 {}개 추출", twitterData.size());
        } catch (Exception e) {
            log.warn("Twitter Card 파싱 실패", e);
        }
        
        return twitterData;
    }
    
    /**
     * 통합 메타데이터 추출 (1차 전략)
     */
    public Map<String, Object> extractStandardMetadata(WebDriver driver) {
        Map<String, Object> metadata = new HashMap<>();
        
        // 1. JSON-LD 우선 시도
        Optional<Map<String, Object>> jsonLd = parseJsonLd(driver);
        if (jsonLd.isPresent()) {
            metadata.put("jsonld", jsonLd.get());
            metadata.put("source", "JSON-LD");
            return metadata;
        }
        
        // 2. OpenGraph 시도
        Map<String, String> openGraph = parseOpenGraph(driver);
        if (!openGraph.isEmpty()) {
            metadata.put("opengraph", openGraph);
            metadata.put("source", "OpenGraph");
        }
        
        // 3. Twitter Card 시도
        Map<String, String> twitterCard = parseTwitterCard(driver);
        if (!twitterCard.isEmpty()) {
            metadata.put("twitter", twitterCard);
            if (!metadata.containsKey("source")) {
                metadata.put("source", "Twitter Card");
            }
        }
        
        return metadata;
    }
    
    private Map<String, Object> parseJsonLdContent(String jsonContent) {
        // 실제로는 Jackson이나 Gson 사용해야 함
        Map<String, Object> result = new HashMap<>();
        
        // 간단한 파싱 로직 (실제 구현에서는 JSON 라이브러리 사용)
        if (jsonContent.contains("\"@type\"")) {
            if (jsonContent.contains("\"Product\"")) {
                result.put("@type", "Product");
                // 더 정교한 JSON 파싱 필요
            }
        }
        
        return result;
    }
    
    private boolean isProductData(Map<String, Object> data) {
        return "Product".equals(data.get("@type")) || 
               data.containsKey("name") || 
               data.containsKey("price");
    }
}
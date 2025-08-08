package com.stylemycloset.cloth.parser;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Component
public class RuleBasedMapper {
    
    @Data
    public static class ExtractionRule {
        private String field;           // 필드명 (예: "productName")
        private String strategy;        // 전략 (css, xpath, jsonpath)
        private String selector;        // 선택자
        private String attribute;       // 속성 (text, href, src, content 등)
        private String regex;           // 정규식 후처리
        private String defaultValue;    // 기본값
        private int priority;           // 우선순위 (낮을수록 먼저 시도)
    }
    
    @Data
    public static class SiteRule {
        private String siteName;
        private String urlPattern;
        private List<ExtractionRule> rules;
        private Map<String, String> categoryMapping;
        private Map<String, List<String>> sizeMapping;
    }
    
    // 설정 기반 사이트 규칙들
    private final Map<String, SiteRule> siteRules = initializeSiteRules();
    
    /**
     * 3차: 설정 기반 CSS/XPath 매핑으로 데이터 추출
     */
    public Map<String, String> extractByRules(WebDriver driver, String url) {
        Map<String, String> extractedData = new HashMap<>();
        
        // URL로 사이트 규칙 찾기
        SiteRule siteRule = findSiteRule(url);
        if (siteRule == null) {
            log.warn("매칭되는 사이트 규칙이 없음: {}", url);
            return extractedData;
        }
        
        log.info("사이트 규칙 적용: {} ({}개 규칙)", siteRule.getSiteName(), siteRule.getRules().size());
        
        // 각 필드별로 규칙 적용 (우선순위 순)
        Map<String, List<ExtractionRule>> rulesByField = groupRulesByField(siteRule.getRules());
        
        for (Map.Entry<String, List<ExtractionRule>> entry : rulesByField.entrySet()) {
            String fieldName = entry.getKey();
            List<ExtractionRule> rules = entry.getValue();
            
            // 우선순위 순으로 정렬
            rules.sort(Comparator.comparing(ExtractionRule::getPriority));
            
            // 첫 번째 성공하는 규칙 사용
            for (ExtractionRule rule : rules) {
                Optional<String> value = extractByRule(driver, rule);
                if (value.isPresent()) {
                    extractedData.put(fieldName, value.get());
                    log.debug("필드 {} 추출 성공: {}", fieldName, value.get());
                    break;
                }
            }
            
            // 추출 실패 시 기본값 사용
            if (!extractedData.containsKey(fieldName)) {
                ExtractionRule firstRule = rules.get(0);
                if (firstRule.getDefaultValue() != null) {
                    extractedData.put(fieldName, firstRule.getDefaultValue());
                }
            }
        }
        
        return extractedData;
    }
    
    private Optional<String> extractByRule(WebDriver driver, ExtractionRule rule) {
        try {
            WebElement element = null;
            
            // 전략별 요소 찾기
            switch (rule.getStrategy().toLowerCase()) {
                case "css":
                    element = driver.findElement(By.cssSelector(rule.getSelector()));
                    break;
                case "xpath":
                    element = driver.findElement(By.xpath(rule.getSelector()));
                    break;
                case "id":
                    element = driver.findElement(By.id(rule.getSelector()));
                    break;
                case "name":
                    element = driver.findElement(By.name(rule.getSelector()));
                    break;
                default:
                    log.warn("지원하지 않는 전략: {}", rule.getStrategy());
                    return Optional.empty();
            }
            
            if (element == null) {
                return Optional.empty();
            }
            
            // 속성별 값 추출
            String value = extractValueFromElement(element, rule.getAttribute());
            
            // 정규식 후처리
            if (rule.getRegex() != null && value != null) {
                value = applyRegex(value, rule.getRegex());
            }
            
            return value != null && !value.trim().isEmpty() ? 
                   Optional.of(value.trim()) : Optional.empty();
                   
        } catch (Exception e) {
            log.debug("규칙 적용 실패: {} - {}", rule.getSelector(), e.getMessage());
            return Optional.empty();
        }
    }
    
    private String extractValueFromElement(WebElement element, String attribute) {
        if (attribute == null || "text".equals(attribute)) {
            return element.getText();
        }
        
        return switch (attribute.toLowerCase()) {
            case "content" -> element.getAttribute("content");
            case "value" -> element.getAttribute("value");
            case "href" -> element.getAttribute("href");
            case "src" -> element.getAttribute("src");
            case "title" -> element.getAttribute("title");
            case "alt" -> element.getAttribute("alt");
            case "data-price" -> element.getAttribute("data-price");
            default -> element.getAttribute(attribute);
        };
    }
    
    private String applyRegex(String input, String regexPattern) {
        try {
            Pattern pattern = Pattern.compile(regexPattern);
            java.util.regex.Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                return matcher.group(1); // 첫 번째 캡처 그룹
            }
        } catch (Exception e) {
            log.warn("정규식 적용 실패: {} on {}", regexPattern, input);
        }
        return input;
    }
    
    private SiteRule findSiteRule(String url) {
        for (SiteRule rule : siteRules.values()) {
            if (url.matches(rule.getUrlPattern())) {
                return rule;
            }
        }
        return null;
    }
    
    private Map<String, List<ExtractionRule>> groupRulesByField(List<ExtractionRule> rules) {
        Map<String, List<ExtractionRule>> grouped = new HashMap<>();
        for (ExtractionRule rule : rules) {
            grouped.computeIfAbsent(rule.getField(), k -> new ArrayList<>()).add(rule);
        }
        return grouped;
    }
    
    /**
     * 사이트별 추출 규칙 초기화
     */
    private Map<String, SiteRule> initializeSiteRules() {
        Map<String, SiteRule> rules = new HashMap<>();
        
        // 무신사 규칙
        SiteRule musinsaRule = new SiteRule();
        musinsaRule.setSiteName("무신사");
        musinsaRule.setUrlPattern(".*musinsa\\.com.*");
        musinsaRule.setRules(Arrays.asList(
            createRule("productName", "css", "meta[property='og:title']", "content", "(.+?)\\s*-\\s*사이즈.*", "무신사 상품", 1),
            createRule("productName", "css", ".prod_buy_header h3", "text", null, "무신사 상품", 2),
            createRule("brand", "css", "meta[property='product:brand']", "content", null, "브랜드 없음", 1),
            createRule("price", "css", "meta[property='product:price:amount']", "content", null, "0", 1),
            createRule("originalPrice", "css", "meta[property='product:price:normal_price']", "content", null, null, 1),
            createRule("discountRate", "css", "meta[property='product:price:sale_rate']", "content", null, null, 1)
        ));
        rules.put("musinsa", musinsaRule);
        
        // 29cm 규칙
        SiteRule cm29Rule = new SiteRule();
        cm29Rule.setSiteName("29CM");
        cm29Rule.setUrlPattern(".*29cm\\.co\\.kr.*");
        cm29Rule.setRules(Arrays.asList(
            createRule("productName", "css", "h1[data-testid='product-title']", "text", null, "29CM 상품", 1),
            createRule("productName", "css", "h1.css-1v99tuv", "text", null, "29CM 상품", 2),
            createRule("productName", "css", ".product-title h1", "text", null, "29CM 상품", 3),
            createRule("brand", "css", "[data-testid='product-brand']", "text", null, "브랜드 없음", 1),
            createRule("brand", "css", ".css-19k8rnp", "text", null, "브랜드 없음", 2),
            createRule("price", "css", "[data-testid='product-price']", "text", "([0-9,]+)", "0", 1),
            createRule("price", "css", ".css-1w96w8a", "text", "([0-9,]+)", "0", 2)
        ));
        rules.put("29cm", cm29Rule);
        
        // 지그재그 규칙
        SiteRule zigzagRule = new SiteRule();
        zigzagRule.setSiteName("지그재그");
        zigzagRule.setUrlPattern(".*zigzag\\.kr.*");
        zigzagRule.setRules(Arrays.asList(
            createRule("productName", "css", ".item-title", "text", null, "지그재그 상품", 1),
            createRule("brand", "css", ".brand-name", "text", null, "브랜드 없음", 1),
            createRule("price", "css", ".sale-price", "text", "([0-9,]+)", "0", 1),
            createRule("originalPrice", "css", ".original-price", "text", "([0-9,]+)", null, 1)
        ));
        rules.put("zigzag", zigzagRule);
        
        return rules;
    }
    
    private ExtractionRule createRule(String field, String strategy, String selector, 
                                    String attribute, String regex, String defaultValue, int priority) {
        ExtractionRule rule = new ExtractionRule();
        rule.setField(field);
        rule.setStrategy(strategy);
        rule.setSelector(selector);
        rule.setAttribute(attribute);
        rule.setRegex(regex);
        rule.setDefaultValue(defaultValue);
        rule.setPriority(priority);
        return rule;
    }
}
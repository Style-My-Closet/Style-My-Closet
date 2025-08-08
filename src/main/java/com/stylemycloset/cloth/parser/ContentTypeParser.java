package com.stylemycloset.cloth.parser;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class ContentTypeParser {
    
    /**
     * JavaScript 변수에서 데이터 추출 (SPA 사이트용)
     */
    public Optional<Map<String, Object>> parseJavaScriptData(WebDriver driver, String[] variablePaths) {
        try {
            JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
            Map<String, Object> jsData = new HashMap<>();
            
            for (String path : variablePaths) {
                try {
                    Object result = jsExecutor.executeScript("return " + path);
                    if (result != null) {
                        jsData.put(path, result);
                        log.debug("JS 변수 추출 성공: {} = {}", path, result);
                    }
                } catch (Exception e) {
                    log.debug("JS 변수 추출 실패: {}", path);
                }
            }
            
            if (!jsData.isEmpty()) {
                log.info("JavaScript 데이터 {}개 추출", jsData.size());
                return Optional.of(jsData);
            }
        } catch (Exception e) {
            log.warn("JavaScript 데이터 파싱 실패", e);
        }
        
        return Optional.empty();
    }
    
    /**
     * API 응답 JSON 추출 (AJAX 사이트용)
     */
    public Optional<Map<String, Object>> parseApiResponse(WebDriver driver) {
        try {
            // Network 로그에서 API 응답 추출하는 로직
            // 실제로는 Chrome DevTools Protocol 사용해야 함
            JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
            
            // 페이지에서 fetch나 XMLHttpRequest 응답 캐치
            String script = """
                return window.__CAPTURED_API_RESPONSES__ || {};
                """;
            
            Object result = jsExecutor.executeScript(script);
            if (result instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> apiData = (Map<String, Object>) result;
                if (!apiData.isEmpty()) {
                    log.info("API 응답 데이터 추출 성공");
                    return Optional.of(apiData);
                }
            }
        } catch (Exception e) {
            log.warn("API 응답 파싱 실패", e);
        }
        
        return Optional.empty();
    }
    
    /**
     * XML/RSS 피드 파싱
     */
    public Optional<Map<String, Object>> parseXmlFeed(WebDriver driver) {
        try {
            // XML content-type 체크
            String contentType = (String) ((JavascriptExecutor) driver)
                .executeScript("return document.contentType");
            
            if (contentType != null && 
                (contentType.contains("xml") || contentType.contains("rss"))) {
                
                String pageSource = driver.getPageSource();
                Map<String, Object> xmlData = parseXmlContent(pageSource);
                
                if (!xmlData.isEmpty()) {
                    log.info("XML 데이터 파싱 성공");
                    return Optional.of(xmlData);
                }
            }
        } catch (Exception e) {
            log.warn("XML 파싱 실패", e);
        }
        
        return Optional.empty();
    }
    
    /**
     * 마이크로데이터 파싱 (schema.org)
     */
    public Map<String, Object> parseMicrodata(WebDriver driver) {
        Map<String, Object> microdata = new HashMap<>();
        
        try {
            // itemscope, itemtype 속성을 가진 요소들 찾기
            List<WebElement> itemScopeElements = driver.findElements(
                By.cssSelector("[itemscope][itemtype]"));
            
            for (WebElement element : itemScopeElements) {
                String itemType = element.getAttribute("itemtype");
                if (itemType != null && itemType.contains("schema.org/Product")) {
                    Map<String, String> properties = extractMicrodataProperties(element);
                    microdata.put("microdata", properties);
                    microdata.put("itemtype", itemType);
                    log.info("마이크로데이터 발견: {}", itemType);
                    break;
                }
            }
        } catch (Exception e) {
            log.warn("마이크로데이터 파싱 실패", e);
        }
        
        return microdata;
    }
    
    /**
     * 통합 컨텐츠 파싱 (2차 전략)
     */
    public Map<String, Object> parseContentByType(WebDriver driver, String siteType) {
        Map<String, Object> contentData = new HashMap<>();
        
        // 사이트 타입별 JavaScript 변수 경로 정의
        String[] jsVariables = getJavaScriptVariablesForSite(siteType);
        
        // 1. JavaScript 변수 시도
        Optional<Map<String, Object>> jsData = parseJavaScriptData(driver, jsVariables);
        if (jsData.isPresent()) {
            contentData.putAll(jsData.get());
            contentData.put("content_source", "JavaScript");
        }
        
        // 2. API 응답 시도
        Optional<Map<String, Object>> apiData = parseApiResponse(driver);
        if (apiData.isPresent()) {
            contentData.putAll(apiData.get());
            contentData.put("api_source", "AJAX");
        }
        
        // 3. 마이크로데이터 시도
        Map<String, Object> microdata = parseMicrodata(driver);
        if (!microdata.isEmpty()) {
            contentData.putAll(microdata);
            contentData.put("microdata_source", "Schema.org");
        }
        
        // 4. XML 피드 시도
        Optional<Map<String, Object>> xmlData = parseXmlFeed(driver);
        if (xmlData.isPresent()) {
            contentData.putAll(xmlData.get());
            contentData.put("xml_source", "RSS/XML");
        }
        
        return contentData;
    }
    
    private String[] getJavaScriptVariablesForSite(String siteType) {
        return switch (siteType.toLowerCase()) {
            case "musinsa" -> new String[]{
                "window.__MSS__.product.state",
                "window.__INITIAL_STATE__.product",
                "window.productData"
            };
            case "29cm" -> new String[]{
                "window.__NEXT_DATA__.props.pageProps",
                "window.__APOLLO_STATE__",
                "window.productInfo"
            };
            case "zigzag" -> new String[]{
                "window.__REDUX_STORE__.getState()",
                "window.pageData.product"
            };
            default -> new String[]{
                "window.productData",
                "window.__INITIAL_STATE__",
                "window.__APP_DATA__"
            };
        };
    }
    
    private Map<String, String> extractMicrodataProperties(WebElement itemScopeElement) {
        Map<String, String> properties = new HashMap<>();
        
        try {
            List<WebElement> propertyElements = itemScopeElement.findElements(
                By.cssSelector("[itemprop]"));
            
            for (WebElement propElement : propertyElements) {
                String propName = propElement.getAttribute("itemprop");
                String propValue = propElement.getAttribute("content");
                if (propValue == null) {
                    propValue = propElement.getText();
                }
                if (propName != null && propValue != null) {
                    properties.put(propName, propValue);
                }
            }
        } catch (Exception e) {
            log.warn("마이크로데이터 속성 추출 실패", e);
        }
        
        return properties;
    }
    
    private Map<String, Object> parseXmlContent(String xmlContent) {
        Map<String, Object> xmlData = new HashMap<>();
        
        // 간단한 XML 파싱 로직 (실제로는 DOM parser 사용)
        if (xmlContent.contains("<item>") || xmlContent.contains("<product>")) {
            xmlData.put("hasProductData", true);
            // 실제 XML 파싱 로직 구현 필요
        }
        
        return xmlData;
    }
}
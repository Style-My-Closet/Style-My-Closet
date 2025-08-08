package com.stylemycloset.cloth.parser;

import com.stylemycloset.cloth.dto.response.ClothExtractionResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FallbackParsingPipeline {
    
    private final MetadataParser metadataParser;
    private final ContentTypeParser contentTypeParser;
    private final RuleBasedMapper ruleBasedMapper;
    private final DomainMapper domainMapper;
    
    /**
     * 계단식 파싱 파이프라인 실행
     * 1차 -> 2차 -> 3차 -> 4차 순으로 실행
     */
    public ClothExtractionResponseDto executeParsingPipeline(WebDriver driver, String url) {
        log.info("계단식 파싱 파이프라인 시작: {}", url);
        
        try {
            // 1차: 표준화된 메타데이터 추출
            log.info("1차: 메타데이터 파싱 시작");
            Map<String, Object> metadata = metadataParser.extractStandardMetadata(driver);
            log.info("1차 완료: {} 항목 추출 (source: {})", 
                    metadata.size(), metadata.get("source"));
            
            // 2차: 컨텐츠 타입별 파싱
            log.info("2차: 컨텐츠 타입 파싱 시작");
            String siteType = extractSiteType(url);
            Map<String, Object> contentData = contentTypeParser.parseContentByType(driver, siteType);
            log.info("2차 완료: {} 항목 추출", contentData.size());
            
            // 3차: 설정 기반 매핑
            log.info("3차: 규칙 기반 매핑 시작");
            Map<String, String> ruleData = ruleBasedMapper.extractByRules(driver, url);
            log.info("3차 완료: {} 항목 추출", ruleData.size());
            
            // 4차: 도메인 모델 매핑
            log.info("4차: 도메인 모델 매핑 시작");
            ClothExtractionResponseDto result = domainMapper.mapToDomainModel(
                metadata, contentData, ruleData, url);
            log.info("4차 완료: 상품명={}, 브랜드={}, 가격={}", 
                    result.getProductName(), result.getBrand(), result.getPrice());
            
            // 성공률 로깅
            logExtractionStats(metadata, contentData, ruleData, result);
            
            return result;
            
        } catch (Exception e) {
            log.error("파싱 파이프라인 실행 중 오류 발생: {}", url, e);
            return ClothExtractionResponseDto.createFailureResponse(url);
        }
    }
    
    /**
     * URL에서 사이트 타입 추출
     */
    private String extractSiteType(String url) {
        if (url.contains("musinsa.com")) return "musinsa";
        if (url.contains("29cm.co.kr")) return "29cm";
        if (url.contains("zigzag.kr")) return "zigzag";
        if (url.contains("ssfshop.com")) return "ssfshop";
        if (url.contains("wconcept.co.kr")) return "wconcept";
        return "unknown";
    }
    
    /**
     * 추출 성공률 통계 로깅
     */
    private void logExtractionStats(Map<String, Object> metadata, 
                                   Map<String, Object> contentData,
                                   Map<String, String> ruleData,
                                   ClothExtractionResponseDto result) {
        
        StringBuilder stats = new StringBuilder();
        stats.append("\n=== 파싱 파이프라인 결과 ===\n");
        
        // 각 단계별 기여도
        stats.append("1차 메타데이터: ").append(metadata.size()).append("개 필드\n");
        stats.append("2차 컨텐츠: ").append(contentData.size()).append("개 필드\n");
        stats.append("3차 규칙 기반: ").append(ruleData.size()).append("개 필드\n");
        
        // 핵심 필드 추출 성공 여부
        stats.append("상품명: ").append(getSuccessStatus(result.getProductName())).append("\n");
        stats.append("브랜드: ").append(getSuccessStatus(result.getBrand())).append("\n");
        stats.append("가격: ").append(result.getPrice().compareTo(java.math.BigDecimal.ZERO) > 0 ? "SUCCESS" : "FAILED").append("\n");
        stats.append("이미지: ").append(result.getImages().size()).append("개\n");
        
        // 데이터 소스
        if (metadata.containsKey("source")) {
            stats.append("주요 데이터 소스: ").append(metadata.get("source")).append("\n");
        }
        
        stats.append("=======================");
        
        log.info(stats.toString());
    }
    
    private String getSuccessStatus(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "FAILED";
        }
        if (value.contains("없음") || value.contains("실패")) {
            return "DEFAULT";
        }
        return "SUCCESS";
    }
    
    /**
     * 파이프라인 성능 메트릭 (향후 확장용)
     */
    public static class PipelineMetrics {
        private long metadataParsingTime;
        private long contentParsingTime;
        private long ruleBasedParsingTime;
        private long domainMappingTime;
        private int totalFieldsExtracted;
        private int successfulFields;
        
        // getters, setters, 통계 메소드들...
    }
}
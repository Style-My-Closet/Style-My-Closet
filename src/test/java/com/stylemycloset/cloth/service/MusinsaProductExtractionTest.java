package com.stylemycloset.cloth.service;

import com.stylemycloset.cloth.dto.response.ClothExtractionResponseDto;
import com.stylemycloset.cloth.service.impl.ClothProductExtractionServiceImpl;
import com.stylemycloset.testutil.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
class MusinsaProductExtractionTest extends IntegrationTestSupport {

    @Autowired
    private ClothProductExtractionServiceImpl service;

    @Test
    void 무신사_상품_5240840_크롤링_테스트() {
        // given
        String productUrl = "https://www.musinsa.com/products/5240840";
        
        System.out.println("=== 무신사 상품 크롤링 시작 ===");
        System.out.println("URL: " + productUrl);
        
        try {
            // when
            ClothExtractionResponseDto result = service.extractProductInfoFromUrl(productUrl);
            
            // then
            System.out.println("=== 추출 결과 ===");
            System.out.println("상품명: " + result.getProductName());
            System.out.println("브랜드: " + result.getBrand());
            System.out.println("카테고리: " + result.getCategory());
            System.out.println("가격: " + result.getPrice());
            System.out.println("원가: " + result.getOriginalPrice());
            System.out.println("할인율: " + result.getDiscountRate());
            System.out.println("색상: " + result.getColors());
            System.out.println("사이즈: " + result.getSizes());
            System.out.println("소재: " + result.getMaterial());
            System.out.println("이미지 개수: " + result.getImages().size());
            System.out.println("재고여부: " + result.getIsAvailable());
            System.out.println("설명: " + result.getDescription());
            
            assertThat(result).isNotNull();
            assertThat(result.getProductName()).isNotEmpty();
            assertThat(result.getCategory()).isEqualTo("TOP");
            
        } catch (Exception e) {
            System.out.println("=== 예외 발생 ===");
            System.out.println("예외 타입: " + e.getClass().getSimpleName());
            System.out.println("예외 메시지: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("원인: " + e.getCause().getMessage());
            }
            e.printStackTrace();
        }
    }

    @Test 
    void 서킷브레이커_테스트_빠른실패() {
        // given - 무신사 사이트이지만 접근할 수 없는 URL로 실제 크롤링에서 실패 유도
        String musinsaValidButFailUrl = "https://www.musinsa.com/products/99999999999";
        
        System.out.println("=== 서킷 브레이커 테스트 시작 (slidingWindowSize=5로 빠른 테스트) ===");
        System.out.println("URL: " + musinsaValidButFailUrl);
        
        try {
            // when - 설정에 따라 5번 호출하면 서킷 브레이커 트리거 (50% 실패율)
            for (int i = 1; i <= 8; i++) {
                System.out.println("--- " + i + "번째 시도 ---");
                long startTime = System.currentTimeMillis();
                
                ClothExtractionResponseDto result = service.extractProductInfoFromUrl(musinsaValidButFailUrl);
                
                long endTime = System.currentTimeMillis();
                System.out.println("소요 시간: " + (endTime - startTime) + "ms");
                System.out.println("결과 상품명: " + result.getProductName());
                System.out.println("결과 설명: " + result.getDescription());
                
                // 시간이 매우 짧으면 fallback이 호출된 것
                if (endTime - startTime < 200) {
                    System.out.println("🔴 서킷 브레이커 fallback 호출 가능성!");
                }
                
                // 서킷이 열렸으면 이후 호출은 매우 빨라질 것
                if (i >= 6 && endTime - startTime < 50) {
                    System.out.println("✅ 서킷 브레이커가 열려서 fallback 호출됨!");
                    break; // 서킷 브레이커 동작 확인되면 종료
                }
                
                // 대기 시간 없이 바로 다음 호출
                Thread.sleep(50);
            }
        } catch (Exception e) {
            System.out.println("=== 예외 발생 ===");
            System.out.println("예외 타입: " + e.getClass().getSimpleName());
            System.out.println("예외 메시지: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("원인: " + e.getCause().getMessage());
            }
        }
        
        System.out.println("=== 서킷 브레이커 테스트 완료 ===");
    }

    @Test 
    void 서킷브레이커_테스트_네트워크오류() {
        // given - 존재하지 않는 도메인으로 네트워크 오류 유도
        String networkErrorUrl = "https://nonexistent-domain-12345.com/products/123";
        
        System.out.println("=== 서킷 브레이커 테스트 시작 (네트워크 오류) ===");
        System.out.println("URL: " + networkErrorUrl);
        
        try {
            // when - 여러 번 호출해서 서킷 브레이커 트리거
            for (int i = 1; i <= 10; i++) {
                System.out.println("--- " + i + "번째 시도 ---");
                long startTime = System.currentTimeMillis();
                
                ClothExtractionResponseDto result = service.extractProductInfoFromUrl(networkErrorUrl);
                
                long endTime = System.currentTimeMillis();
                System.out.println("소요 시간: " + (endTime - startTime) + "ms");
                System.out.println("결과 상품명: " + result.getProductName());
                System.out.println("결과 설명: " + result.getDescription());
                
                // 시간이 매우 짧으면 fallback이 호출된 것
                if (endTime - startTime < 200) {
                    System.out.println("🔴 서킷 브레이커 fallback 호출 가능성!");
                }
                
                // 서킷이 열렸으면 이후 호출은 매우 빨라질 것
                if (i >= 6 && endTime - startTime < 50) {
                    System.out.println("✅ 서킷 브레이커가 열려서 fallback 호출됨!");
                    break; // 서킷 브레이커 동작 확인되면 종료
                }
                
                System.out.println();
            }
        } catch (Exception e) {
            System.out.println("=== 예외 발생 ===");
            System.out.println("예외 타입: " + e.getClass().getSimpleName());
            System.out.println("예외 메시지: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("원인: " + e.getCause().getMessage());
            }
        }
        
        System.out.println("=== 서킷 브레이커 테스트 완료 ===");
    }

    @Test 
    void 서킷브레이커_테스트_타임아웃오류() {
        // given - 응답이 매우 느린 서버로 타임아웃 유도
        String timeoutUrl = "https://httpbin.org/delay/10"; // 10초 지연
        
        System.out.println("=== 서킷 브레이커 테스트 시작 (타임아웃 오류) ===");
        System.out.println("URL: " + timeoutUrl);
        
        try {
            // when - 여러 번 호출해서 서킷 브레이커 트리거
            for (int i = 1; i <= 6; i++) {
                System.out.println("--- " + i + "번째 시도 ---");
                long startTime = System.currentTimeMillis();
                
                ClothExtractionResponseDto result = service.extractProductInfoFromUrl(timeoutUrl);
                
                long endTime = System.currentTimeMillis();
                System.out.println("소요 시간: " + (endTime - startTime) + "ms");
                System.out.println("결과 상품명: " + result.getProductName());
                System.out.println("결과 설명: " + result.getDescription());
                
                // 시간이 매우 짧으면 fallback이 호출된 것
                if (endTime - startTime < 200) {
                    System.out.println("🔴 서킷 브레이커 fallback 호출 가능성!");
                }
                
                // 서킷이 열렸으면 이후 호출은 매우 빨라질 것
                if (i >= 4 && endTime - startTime < 50) {
                    System.out.println("✅ 서킷 브레이커가 열려서 fallback 호출됨!");
                    break; // 서킷 브레이커 동작 확인되면 종료
                }
                
                System.out.println();
            }
        } catch (Exception e) {
            System.out.println("=== 예외 발생 ===");
            System.out.println("예외 타입: " + e.getClass().getSimpleName());
            System.out.println("예외 메시지: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("원인: " + e.getCause().getMessage());
            }
        }
        
        System.out.println("=== 서킷 브레이커 테스트 완료 ===");
    }
}
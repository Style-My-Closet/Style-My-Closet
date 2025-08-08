package com.stylemycloset.cloth.service.impl;

import com.stylemycloset.binarycontent.service.ImageDownloadService;
import com.stylemycloset.cloth.adapter.SiteDataAdapter;
import com.stylemycloset.cloth.dto.RawSiteData;
import com.stylemycloset.cloth.dto.response.ClothExtractionResponseDto;
import com.stylemycloset.cloth.dto.ClothResponseDto;
import com.stylemycloset.cloth.dto.AttributeDto;
import com.stylemycloset.cloth.entity.ClothingCategoryType;
import com.stylemycloset.cloth.exception.ClothesException;
import com.stylemycloset.cloth.exception.ClothingErrorCode;
import com.stylemycloset.cloth.parser.FallbackParsingPipeline;
import com.stylemycloset.cloth.service.ClothProductExtractionService;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClothProductExtractionServiceImpl implements ClothProductExtractionService {

    private final RestTemplate restTemplate;
    private final Random random = new Random();
    private final List<SiteDataAdapter> adapters;
    private final FallbackParsingPipeline parsingPipeline;
    private final ImageDownloadService imageDownloadService;

    @Override
    @CircuitBreaker(name = "crawling", fallbackMethod = "fallbackExtraction")
    @RateLimiter(name = "crawling")
    @Retry(name = "crawling")
    public ClothExtractionResponseDto extractProductInfoFromUrl(String productUrl) {
        WebDriver driver = null;
        try {

            // URL 유효성 검사
            if (!isValidProductUrl(productUrl)) {
                return ClothExtractionResponseDto.createFailureResponse(productUrl);
            }

            // 계단식 파싱 파이프라인 실행
            ClothExtractionResponseDto extractedInfo = executeParsingPipeline(productUrl);
            
            if (isFailureResult(extractedInfo)) {
                extractedInfo = executeAdapterFallback(productUrl);
            }
            

            // 카테고리 검증
            validateCategory(extractedInfo.getCategory());
            
            // 이미지 다운로드 (비동기적으로 처리하지 않고 바로 실행)
            try {
                if (extractedInfo.getImages() != null && !extractedInfo.getImages().isEmpty()) {
                    imageDownloadService.downloadAndSaveImages(extractedInfo.getImages());
                }
            } catch (Exception e) {
                log.warn("이미지 다운로드 실패했지만 상품 정보는 정상 반환: {}", productUrl, e);
            }
            
            return extractedInfo;
            
        } catch (Exception e) {
            return ClothExtractionResponseDto.createFailureResponse(productUrl);
        } finally {
        }
    }

    @Override
    public ClothResponseDto buildClothResponse(String productUrl) {
        ClothExtractionResponseDto extractedInfo = extractProductInfoFromUrl(productUrl);
        ClothResponseDto response = new ClothResponseDto();
        response.setId(java.util.UUID.randomUUID().toString());
        response.setOwnerId(java.util.UUID.randomUUID().toString());
        response.setName(extractedInfo.getProductName());
        response.setImageUrl(extractedInfo.getProductUrl());
        response.setType(ClothingCategoryType.from(extractedInfo.getCategory()));

        java.util.List<AttributeDto> attributes = new java.util.ArrayList<>();
        if (extractedInfo.getBrand() != null && !extractedInfo.getBrand().isEmpty()) {
            attributes.add(new AttributeDto(null, "브랜드", java.util.List.of(), extractedInfo.getBrand()));
        }
        if (extractedInfo.getColors() != null && !extractedInfo.getColors().isEmpty()) {
            attributes.add(new AttributeDto(null, "색상", extractedInfo.getColors(), null));
        }
        if (extractedInfo.getSizes() != null && !extractedInfo.getSizes().isEmpty()) {
            attributes.add(new AttributeDto(null, "사이즈", extractedInfo.getSizes(), null));
        }
        if (extractedInfo.getMaterial() != null && !extractedInfo.getMaterial().isEmpty()) {
            attributes.add(new AttributeDto(null, "소재", java.util.List.of(), extractedInfo.getMaterial()));
        }
        response.setAttributes(attributes);
        return response;
    }

    public ClothExtractionResponseDto fallbackExtraction(String productUrl, Exception ex) {
        return ClothExtractionResponseDto.builder()
            .productName("상품 정보 추출 실패")
            .brand("알 수 없음")
            .category("기타")
            .price(BigDecimal.ZERO)
            .originalPrice("0")
            .discountRate("0%")
            .colors(List.of("기본색"))
            .sizes(List.of("FREE"))
            .material("알 수 없음")
            .images(List.of())
            .productUrl(productUrl)
            .description("현재 상품 정보를 가져올 수 없습니다. 잠시 후 다시 시도해주세요.")
            .isAvailable(false)
            .build();
    }

    private void validateCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new ClothesException(ClothingErrorCode.INVALID_CATEGORY);
        }
        
        // ClothingCategoryType enum과 매핑 확인
        boolean isValidCategory = false;
        for (ClothingCategoryType categoryType : ClothingCategoryType.values()) {
            // enum 이름과 일치하는지 확인
            if (categoryType.name().equalsIgnoreCase(category)) {
                isValidCategory = true;
                break;
            }
        }
        
        if (!isValidCategory) {
            throw new ClothesException(ClothingErrorCode.INVALID_CATEGORY);
        }
        
    }

    @Override
    public boolean isValidProductUrl(String productUrl) {
        if (productUrl == null || productUrl.trim().isEmpty()) {
            return false;
        }
        
        // 기본적인 URL 형식 검사
        if (!productUrl.matches("^https?://.*")) {
            return false;
        }
        
        // 주요 쇼핑몰 도메인 검사
        String lowerUrl = productUrl.toLowerCase();
        return lowerUrl.contains("musinsa.com") || 
               lowerUrl.contains("zigzag.kr") || 
               lowerUrl.contains("29cm.co.kr") ||
               lowerUrl.contains("ssfshop.com") ||
               lowerUrl.contains("wconcept.co.kr") ||
               lowerUrl.contains("uniquelo.com") ||
               lowerUrl.contains("zara.com") ||
               lowerUrl.contains("hm.com");
    }

    private ClothExtractionResponseDto executeParsingPipeline(String productUrl) {
        WebDriver driver = null;
        try {
            driver = createWebDriver();
            driver.get(productUrl);
            
            // 페이지 로드 대기
            Thread.sleep(3000);
            
            return parsingPipeline.executeParsingPipeline(driver, productUrl);
            
        } catch (Exception e) {
            return ClothExtractionResponseDto.createFailureResponse(productUrl);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private ClothExtractionResponseDto executeAdapterFallback(String productUrl) {
        SiteDataAdapter adapter = findAdapter(productUrl);
        if (adapter == null) {
            return generateDummyProductInfo(productUrl);
        }

        RawSiteData rawData = crawlRawDataWithSelenium(productUrl, adapter);
        return adapter.convert(rawData);
    }
    
    /**
     * 추출 결과가 실패인지 확인
     */
    private boolean isFailureResult(ClothExtractionResponseDto result) {
        return result == null ||
               "추출 실패".equals(result.getProductName()) ||
               (result.getProductName().contains("없음") && 
                result.getPrice().compareTo(BigDecimal.ZERO) == 0);
    }
    
    /**
     * WebDriver 생성 (공통 로직 분리)
     */
    private WebDriver createWebDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        
        return new ChromeDriver(options);
    }


    private SiteDataAdapter findAdapter(String productUrl) {
        return adapters.stream()
                .filter(adapter -> adapter.supports(productUrl))
                .findFirst()
                .orElse(null);
    }

    /**
     * 셀레니움을 사용하여 원시 데이터를 크롤링합니다.
     */
    private RawSiteData crawlRawDataWithSelenium(String productUrl, SiteDataAdapter adapter) {
        WebDriver driver = null;
        try {
            driver = createWebDriver();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            
            // 페이지 로드
            driver.get(productUrl);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            Thread.sleep(5000); // 사이트별 로딩 시간 고려
            
            // 사이트별 크롤링 로직 실행
            if (adapter.getSiteName().equals("무신사")) {
                return extractMusinsaRawData(driver, wait, productUrl);
            } else if (adapter.getSiteName().equals("29CM")) {
                return extract29cmRawData(driver, wait, productUrl);
            } else {
                return RawSiteData.empty(productUrl, adapter.getSiteName());
            }
            
        } catch (Exception e) {
            log.error("원시 데이터 크롤링 실패: {}", productUrl, e);
            return RawSiteData.empty(productUrl, adapter.getSiteName());
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private String extractMetaContent(WebDriver driver, String property) {
        try {
            String selector = String.format("meta[property='%s']", property);
            WebElement metaElement = driver.findElement(By.cssSelector(selector));
            return metaElement.getAttribute("content");
        } catch (Exception e) {
            log.warn("메타 태그 추출 실패: {}", property);
            return null;
        }
    }
    
    private List<String> extractImages(WebDriver driver) {
        List<String> images = new ArrayList<>();
        try {
            // JavaScript에서 이미지 정보 추출
            String script = "return window.__MSS__.product.state.goodsImages.map(img => 'https://image.msscdn.net' + img.imageUrl);";
            Object result = ((JavascriptExecutor) driver).executeScript(script);
            
            if (result instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> imageUrls = (List<String>) result;
                images.addAll(imageUrls);
            }
        } catch (Exception e) {
            log.warn("JavaScript 이미지 추출 실패, CSS 선택자로 대체");
            
            // CSS 선택자로 이미지 추출 (대체 방법)
            try {
                List<WebElement> imgElements = driver.findElements(By.cssSelector("img[src*='image.msscdn.net']"));
                for (WebElement img : imgElements) {
                    String src = img.getAttribute("src");
                    if (src != null && !src.isEmpty()) {
                        images.add(src);
                    }
                }
            } catch (Exception ex) {
                log.warn("CSS 이미지 추출도 실패");
            }
        }
        
        return images.isEmpty() ? List.of("https://via.placeholder.com/400x500?text=이미지+없음") : images;
    }

    /**
     * 무신사 원시 데이터 추출
     */
    private RawSiteData extractMusinsaRawData(WebDriver driver, WebDriverWait wait, String productUrl) {
        Map<String, String> fields = new HashMap<>();
        List<String> images = new ArrayList<>();
        
        try {
            // 메타 태그에서 정보 추출
            fields.put("productName", extractMetaContent(driver, "og:title"));
            fields.put("brand", extractMetaContent(driver, "product:brand"));
            fields.put("price", extractMetaContent(driver, "product:price:amount"));
            fields.put("originalPrice", extractMetaContent(driver, "product:price:normal_price"));
            fields.put("discountRate", extractMetaContent(driver, "product:price:sale_rate"));
            
            // 이미지 추출
            images = extractImages(driver);
            
        } catch (Exception e) {
            log.warn("무신사 원시 데이터 추출 중 오류", e);
        }
        
        return RawSiteData.builder()
                .extractedFields(fields)
                .extractedImages(images)
                .sourceUrl(productUrl)
                .siteName("무신사")
                .build();
    }

    /**
     * 29cm 원시 데이터 추출
     */
    private RawSiteData extract29cmRawData(WebDriver driver, WebDriverWait wait, String productUrl) {
        Map<String, String> fields = new HashMap<>();
        List<String> images = new ArrayList<>();
        
        try {
            // 상품명 추출
            try {
                WebElement titleElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("h1.css-1v99tuv, h1[data-testid='product-title'], .product-title h1, h1.ProductInfo_title__2Eaub")));
                fields.put("productName", titleElement.getText().trim());
            } catch (Exception e) {
                log.warn("29cm 상품명 추출 실패", e);
            }
            
            // 브랜드 추출
            try {
                WebElement brandElement = driver.findElement(By.cssSelector(
                    ".css-19k8rnp, [data-testid='product-brand'], .product-brand, .ProductInfo_brand__1XyM2"));
                fields.put("brand", brandElement.getText().trim());
            } catch (Exception e) {
                log.warn("29cm 브랜드 추출 실패", e);
            }
            
            // 가격 추출
            try {
                WebElement priceElement = driver.findElement(By.cssSelector(
                    ".css-1w96w8a, [data-testid='product-price'], .product-price, .ProductInfo_price__2rM3Y"));
                fields.put("price", priceElement.getText().trim());
            } catch (Exception e) {
                log.warn("29cm 가격 추출 실패", e);
            }
            
            // 이미지 추출
            try {
                List<WebElement> imgElements = driver.findElements(By.cssSelector(
                    ".css-1sw7q4x img, .product-image img, .ProductImage_image__3gQbQ"));
                for (WebElement img : imgElements) {
                    String src = img.getAttribute("src");
                    if (src != null && src.startsWith("http")) {
                        images.add(src);
                    }
                }
            } catch (Exception e) {
                log.warn("29cm 이미지 추출 실패", e);
            }
            
        } catch (Exception e) {
            log.warn("29cm 원시 데이터 추출 중 오류", e);
        }
        
        return RawSiteData.builder()
                .extractedFields(fields)
                .extractedImages(images)
                .sourceUrl(productUrl)
                .siteName("29CM")
                .build();
    }

    /**
     * 개발/테스트용 더미 상품 정보 생성
     */
    private ClothExtractionResponseDto generateDummyProductInfo(String productUrl) {
        String[] brands = {"나이키", "아디다스", "유니클로", "ZARA", "H&M", "무신사"};
        String[] categories = {"상의", "하의", "원피스", "아우터", "신발", "가방"};
        String[] materials = {"면 100%", "폴리에스터 100%", "울 80% 면 20%", "데님", "가죽", "실크"};
        String[] colors = {"블랙", "화이트", "네이비", "그레이", "베이지", "레드"};

        String randomBrand = brands[random.nextInt(brands.length)];
        String randomCategory = categories[random.nextInt(categories.length)];
        String randomMaterial = materials[random.nextInt(materials.length)];
        String randomColor = colors[random.nextInt(colors.length)];
        
        int basePrice = 20000 + random.nextInt(80000); // 2만원 ~ 10만원
        int discountPercent = random.nextInt(30); // 0~30% 할인
        int finalPrice = basePrice * (100 - discountPercent) / 100;
        
        return ClothExtractionResponseDto.builder()
                .productName(randomBrand + " " + randomCategory + " " + randomColor)
                .brand(randomBrand)
                .category(randomCategory)
                .colors(Arrays.asList(randomColor, "화이트", "블랙"))
                .sizes(Arrays.asList("S", "M", "L", "XL"))
                .material(randomMaterial)
                .price(BigDecimal.valueOf(finalPrice))
                .originalPrice(String.valueOf(basePrice))
                .discountRate(discountPercent + "%")
                .images(Arrays.asList(
                    "https://example.com/image1.jpg",
                    "https://example.com/image2.jpg",
                    "https://example.com/image3.jpg"
                ))
                .productUrl(productUrl)
                .description(randomBrand + "의 프리미엄 " + randomCategory + "입니다. " + randomMaterial + " 소재로 제작되어 편안한 착용감을 제공합니다.")
                .isAvailable(true)
                .build();
    }
} 
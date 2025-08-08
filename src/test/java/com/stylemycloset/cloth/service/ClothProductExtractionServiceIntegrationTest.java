package com.stylemycloset.cloth.service;

import com.stylemycloset.binarycontent.BinaryContent;
import com.stylemycloset.binarycontent.BinaryContentRepository;
import com.stylemycloset.binarycontent.service.ImageDownloadService;
import com.stylemycloset.cloth.adapter.SiteDataAdapter;
import com.stylemycloset.cloth.adapter.impl.Cm29DataAdapter;
import com.stylemycloset.cloth.adapter.impl.MusinsaDataAdapter;
import com.stylemycloset.cloth.dto.response.ClothExtractionResponseDto;
import com.stylemycloset.cloth.parser.FallbackParsingPipeline;
import com.stylemycloset.cloth.service.impl.ClothProductExtractionServiceImpl;
import com.stylemycloset.testutil.TestContainerSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Tag("e2e")
@ActiveProfiles("test")
class ClothProductExtractionServiceIntegrationTest extends TestContainerSupport {

    @MockBean
    private RestTemplate restTemplate;
    
    @MockBean
    private FallbackParsingPipeline parsingPipeline;
    
    @MockBean
    private ImageDownloadService imageDownloadService;
    
    @Autowired
    private BinaryContentRepository binaryContentRepository;

    private ClothProductExtractionServiceImpl service;

    @BeforeEach
    void setUp() {
        // 어댑터들을 생성
        List<SiteDataAdapter> adapters = List.of(
            new MusinsaDataAdapter(),
            new Cm29DataAdapter()
        );
        service = new ClothProductExtractionServiceImpl(restTemplate, adapters, parsingPipeline, imageDownloadService);
        
        // 이미지 다운로드 서비스 Mock 설정 - 테스트 메서드에서 개별 설정
    }

    @Test
    void 무신사_URL_유효성_검사_테스트() {
        // Given
        String validMusinsaUrl = "https://www.musinsa.com/products/4055686";
        String invalidUrl = "https://invalid-url.com";

        // When
        boolean isValidMusinsa = service.isValidProductUrl(validMusinsaUrl);
        boolean isValidInvalid = service.isValidProductUrl(invalidUrl);

        // Then
        assertThat(isValidMusinsa).isTrue();
        assertThat(isValidInvalid).isFalse();
    }

    @Test
    void 실제_무신사_URL_크롤링_테스트() {
        // Given
        String musinsaUrl = "https://www.musinsa.com/products/4055686";
        doNothing().when(imageDownloadService).downloadAndSaveImages(anyList());

        // When
        ClothExtractionResponseDto result = service.extractProductInfoFromUrl(musinsaUrl);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProductName()).isNotBlank();
        assertThat(result.getProductName()).isNotEqualTo("추출 실패");
        assertThat(result.getProductUrl()).isEqualTo(musinsaUrl);
        
        // 이미지 다운로드 서비스 호출 검증
        if (result.getImages() != null && !result.getImages().isEmpty()) {
            verify(imageDownloadService).downloadAndSaveImages(result.getImages());
        }

        // 결과 출력
        System.out.println("=== 무신사 크롤링 결과 ===");
        System.out.println("상품명: " + result.getProductName());
        System.out.println("브랜드: " + result.getBrand());
        System.out.println("카테고리: " + result.getCategory());
        System.out.println("가격: " + result.getPrice());
        System.out.println("색상: " + result.getColors());
        System.out.println("사이즈: " + result.getSizes());
        System.out.println("이미지 개수: " + (result.getImages() != null ? result.getImages().size() : 0));
        System.out.println("상품 URL: " + result.getProductUrl());
        System.out.println("이미지 다운로드 서비스 호출됨: " + (result.getImages() != null && !result.getImages().isEmpty()));
        System.out.println("==========================");
    }

    @Test
    void 더미_데이터_테스트() {
        // Given
        String dummyUrl = "https://www.musinsa.com/products/999999"; // 유효한 무신사 URL 형식
        doNothing().when(imageDownloadService).downloadAndSaveImages(anyList());

        // When
        ClothExtractionResponseDto result = service.extractProductInfoFromUrl(dummyUrl);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProductName()).isNotBlank();
        assertThat(result.getBrand()).isNotBlank();
        assertThat(result.getCategory()).isNotBlank();
        assertThat(result.getPrice()).isNotNull();
        assertThat(result.getImages()).isNotEmpty();
        assertThat(result.getProductUrl()).isEqualTo(dummyUrl);
        
        // 이미지 다운로드 서비스 호출 검증
        verify(imageDownloadService).downloadAndSaveImages(result.getImages());
    }

    @Test
    void cm29_신발_URL_유효성_검사_테스트() {
        // Given
        String valid29cmUrl = "https://www.29cm.co.kr/products/2387405";
        String invalid29cmUrl = "https://www.invalidsite.com/products/123";

        // When
        boolean isValid29cm = service.isValidProductUrl(valid29cmUrl);
        boolean isInvalidSite = service.isValidProductUrl(invalid29cmUrl);

        // Then
        assertThat(isValid29cm).isTrue();
        assertThat(isInvalidSite).isFalse();
        
        System.out.println("=== 29cm URL 유효성 검사 결과 ===");
        System.out.println("29cm URL 유효성: " + isValid29cm);
        System.out.println("잘못된 URL 유효성: " + isInvalidSite);
        System.out.println("================================");
    }

    @Test
    void cm29_신발_상품_정보_추출_테스트() {
        // Given
        String shoesUrl = "https://www.29cm.co.kr/products/2387405";
        doNothing().when(imageDownloadService).downloadAndSaveImages(anyList());

        // When
        ClothExtractionResponseDto result = service.extractProductInfoFromUrl(shoesUrl);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProductName()).isNotBlank();
        assertThat(result.getBrand()).isNotBlank();
        assertThat(result.getCategory()).isNotBlank();
        assertThat(result.getPrice()).isNotNull();
        assertThat(result.getImages()).isNotEmpty();
        assertThat(result.getProductUrl()).isEqualTo(shoesUrl);
        assertThat(result.getIsAvailable()).isTrue();
        
        // 이미지 다운로드 서비스 호출 검증
        verify(imageDownloadService).downloadAndSaveImages(result.getImages());

        // 결과 출력
        System.out.println("=== 29cm 신발 상품 정보 추출 결과 ===");
        System.out.println("상품명: " + result.getProductName());
        System.out.println("브랜드: " + result.getBrand());
        System.out.println("카테고리: " + result.getCategory());
        System.out.println("가격: " + result.getPrice());
        System.out.println("원가: " + result.getOriginalPrice());
        System.out.println("할인율: " + result.getDiscountRate());
        System.out.println("색상: " + result.getColors());
        System.out.println("사이즈: " + result.getSizes());
        System.out.println("소재: " + result.getMaterial());
        System.out.println("이미지 개수: " + (result.getImages() != null ? result.getImages().size() : 0));
        System.out.println("상품 URL: " + result.getProductUrl());
        System.out.println("상품 설명: " + result.getDescription());
        System.out.println("이미지 다운로드 서비스 호출됨: " + (result.getImages() != null && !result.getImages().isEmpty()));
        System.out.println("현재는 더미 데이터입니다 (무신사만 실제 크롤링)");
        System.out.println("=====================================");
    }
    
    @Test
    void BinaryContent_소프트_딜리트_기능_테스트() {
        // Given - 테스트용 BinaryContent 생성
        BinaryContent testContent = new BinaryContent(
            "test-image.jpg", 
            "https://example.com/test.jpg",
            "image/jpeg", 
            1024L
        );
        
        // When - 저장 후 소프트 딜리트
        BinaryContent saved = binaryContentRepository.save(testContent);
        System.out.println("저장된 BinaryContent ID: " + saved.getId());
        
        // 소프트 딜리트 실행
        saved.softDelete();
        binaryContentRepository.save(saved);
        
        // Then - @Where 어노테이션으로 인해 일반 조회에서 제외되는지 확인
        List<BinaryContent> allContents = binaryContentRepository.findAll();
        boolean isDeletedContentExcluded = allContents.stream()
            .noneMatch(content -> content.getId().equals(saved.getId()));
        
        assertThat(isDeletedContentExcluded).isTrue();
        System.out.println("소프트 딜리트된 이미지가 일반 조회에서 제외됨: " + isDeletedContentExcluded);
        
        // 직접 ID로 조회 시에도 @Where 조건으로 인해 찾을 수 없어야 함
        boolean notFoundById = binaryContentRepository.findById(saved.getId()).isEmpty();
        assertThat(notFoundById).isTrue();
        System.out.println("소프트 딜리트된 이미지 ID 조회 결과 없음: " + notFoundById);
    }
    
    @Test
    void 무신사_상품_5240840_크롤링_테스트() {
        // given
        String productUrl = "https://www.musinsa.com/products/5240840";
        doNothing().when(imageDownloadService).downloadAndSaveImages(anyList());
        
        System.out.println("=== 무신사 상품 크롤링 시작 ===");
        System.out.println("URL: " + productUrl);
        
        // when
        ClothExtractionResponseDto result = service.extractProductInfoFromUrl(productUrl);
        
        // then
        System.out.println("=== 추출된 상품 정보 ===");
        System.out.println("상품명: " + result.getProductName());
        System.out.println("브랜드: " + result.getBrand());
        System.out.println("카테고리: " + result.getCategory());
        System.out.println("가격: " + result.getPrice());
        System.out.println("원가: " + result.getOriginalPrice());
        System.out.println("할인율: " + result.getDiscountRate());
        System.out.println("소재: " + result.getMaterial());
        System.out.println("재고여부: " + result.getIsAvailable());
        System.out.println("설명: " + result.getDescription());
        System.out.println("색상: " + result.getColors());
        System.out.println("사이즈: " + result.getSizes());
        System.out.println("이미지 개수: " + (result.getImages() != null ? result.getImages().size() : 0));
        if (result.getImages() != null && !result.getImages().isEmpty()) {
            System.out.println("첫 번째 이미지: " + result.getImages().get(0));
        }
        
        // 기본 검증
        assertThat(result).isNotNull();
        assertThat(result.getProductName()).isNotEmpty();
        assertThat(result.getBrand()).isNotEmpty();
        assertThat(result.getCategory()).isNotEmpty();
        
        // 이미지 다운로드 서비스 호출 확인
        verify(imageDownloadService).downloadAndSaveImages(result.getImages());
    }

    @Test
    void BinaryContent_저장_및_조회_기능_테스트() {
        // Given
        BinaryContent content1 = new BinaryContent("image1.jpg", "https://example.com/1.jpg", "image/jpeg", 2048L);
        BinaryContent content2 = new BinaryContent("image2.png", "https://example.com/2.png", "image/png", 1536L);
        
        // When
        BinaryContent saved1 = binaryContentRepository.save(content1);
        BinaryContent saved2 = binaryContentRepository.save(content2);
        
        // Then
        assertThat(saved1.getId()).isNotNull();
        assertThat(saved2.getId()).isNotNull();
        assertThat(saved1.getFileName()).isEqualTo("image1.jpg");
        assertThat(saved2.getFileName()).isEqualTo("image2.png");
        
        // 전체 조회 테스트
        List<BinaryContent> allContents = binaryContentRepository.findAll();
        assertThat(allContents.size()).isGreaterThanOrEqualTo(2);
        
        System.out.println("=== BinaryContent 저장 테스트 결과 ===");
        System.out.println("저장된 Content 1 ID: " + saved1.getId());
        System.out.println("저장된 Content 2 ID: " + saved2.getId());
        System.out.println("전체 BinaryContent 개수: " + allContents.size());
        System.out.println("======================================");
    }
} 
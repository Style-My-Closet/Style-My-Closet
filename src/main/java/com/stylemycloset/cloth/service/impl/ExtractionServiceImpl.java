package com.stylemycloset.cloth.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stylemycloset.binarycontent.entity.BinaryContent;
import com.stylemycloset.binarycontent.storage.BinaryContentStorage;
import com.stylemycloset.binarycontent.service.ImageDownloadService;
import com.stylemycloset.cloth.dto.ClothCreateRequestDto;
import com.stylemycloset.cloth.dto.ClothResponseDto;
import com.stylemycloset.cloth.dto.response.ClothExtractionResponseDto;
import com.stylemycloset.cloth.parser.ImageAiParser;
import com.stylemycloset.cloth.service.ClothProductExtractionService;
import com.stylemycloset.cloth.service.ClothService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExtractionServiceImpl implements ClothProductExtractionService {

    private final RestTemplate restTemplate;
    private final ImageDownloadService imageDownloadService;
    private final ClothService clothService;
    private final com.stylemycloset.cloth.service.ImageVisionService imageVisionService;
    private final BinaryContentStorage binaryContentStorage;

    @Override
    public ClothResponseDto extractAndSave(String productUrl, Long userId) {
        // 1) 빠른 HTML 파싱으로 최소 정보 확보
        ClothExtractionResponseDto extracted = extractMinimalInfo(productUrl);

        // 2) 기본 의류 생성 (이미지 없이)
        ClothCreateRequestDto createRequest = buildCreateRequest(extracted);
        ClothResponseDto created = clothService.createCloth(createRequest, userId);
        Long clothId = created.getId();

        // 3) 병렬 처리: AI 분석 + 이미지 저장
        String originalImageUrl = getOriginalImageUrl(extracted);
        if (originalImageUrl != null) {
            // 3-1) AI 분석 (원본 무신사 URL 사용)
            CompletableFuture<Void> aiAnalysis = CompletableFuture.runAsync(() -> {
                try {
                    String json = imageVisionService.analyzeImageToJson(originalImageUrl);
                    if (json != null && !json.contains("AI_DISABLED") && !json.contains("AI_HTTP_ERROR")) {
                        var root = ImageAiParser.extractContentJson(json);
                        var attrs = ImageAiParser.mapAttributes(root);
                        clothService.upsertAttributesByName(clothId, attrs);
                    }
                } catch (Exception e) {
                    log.debug("AI 분석 실패: {}", e.getMessage());
                }
            });

            // 3-2) 이미지 다운로드 및 S3 저장
            CompletableFuture<BinaryContent> imageStorage = CompletableFuture.supplyAsync(() -> {
                try {
                    java.util.List<BinaryContent> saved = imageDownloadService.downloadAndSaveImages(List.of(originalImageUrl));
                    if (!saved.isEmpty()) {
                        // Cloth 엔티티에 이미지 ID 업데이트
                        clothService.updateClothImage(clothId, saved.getFirst().getId());
                        return saved.getFirst();
                    }
                } catch (Exception e) {
                    log.debug("이미지 저장 실패: {}", e.getMessage());
                }
                return null;
            });

            // 4) 두 작업 완료 대기 (타임아웃 설정)
            try {
                CompletableFuture.allOf(aiAnalysis, imageStorage).get(30, java.util.concurrent.TimeUnit.SECONDS);
                
                // 저장된 이미지가 있으면 presigned URL로 응답 업데이트
                BinaryContent savedImage = imageStorage.get();
                if (savedImage != null) {
                    try {
                        java.net.URL presigned = binaryContentStorage.getUrl(savedImage.getId());
                        if (presigned != null) {
                            created.setImageUrl(presigned.toString());
                        }
                    } catch (Exception ignore) {}
                }
                
                // AI 분석 결과 반영된 최신 데이터 다시 조회
                created = clothService.getClothResponseById(clothId);
                if (savedImage != null) {
                    try {
                        java.net.URL presigned = binaryContentStorage.getUrl(savedImage.getId());
                        if (presigned != null) {
                            created.setImageUrl(presigned.toString());
                        }
                    } catch (Exception ignore) {}
                }
            } catch (Exception e) {
                log.debug("병렬 처리 타임아웃 또는 실패: {}", e.getMessage());
            }
        }

        return created;
    }

    private String getOriginalImageUrl(ClothExtractionResponseDto extracted) {
        if (extracted != null && extracted.getImages() != null && !extracted.getImages().isEmpty()) {
            String firstImage = extracted.getImages().getFirst();
            if (firstImage != null && firstImage.startsWith("http")) {
                return firstImage;
            }
        }
        return null;
    }

    private ClothExtractionResponseDto extractMinimalInfo(String productUrl) {
        ClothExtractionResponseDto ex = fastExtractFromHtml(productUrl);
        return (ex != null) ? ex : ClothExtractionResponseDto.createFailureResponse(productUrl);
    }

    private ClothCreateRequestDto buildCreateRequest(ClothExtractionResponseDto ex) {
        ClothCreateRequestDto req = new ClothCreateRequestDto();
        req.setName(ex != null ? ex.getProductName() : null);
        req.setType(ex != null ? ex.getCategory() : null);
        // 이미지는 비동기로 별도 처리하므로 초기 생성 시에는 제외
        return req;
    }

    private ClothExtractionResponseDto fastExtractFromHtml(String productUrl) {
        try {
            String html = fetchHtml(productUrl);
            if (html == null || html.isBlank()) return null;

            ParsedProduct parsed = new ParsedProduct();
            parseJsonLd(getString(html), parsed);
            fillFallbackMeta(html, parsed);
            return buildFastResponse(productUrl, parsed);
        } catch (Exception e) {
            return null;
        }
    }

    private String fetchHtml(String productUrl) {
        return restTemplate.getForObject(productUrl, String.class);
    }

    private void parseJsonLd(String ld, ParsedProduct parsed) {
        if (ld == null || ld.isBlank()) return;
        try {
            JsonNode meta = new ObjectMapper().readTree(ld.trim());
            parsed.name = meta.path("name").asText("");
            parsed.brand = meta.path("brand").path("name").asText("");
            String p = meta.path("offers").path("price").asText("");
            if (p != null) {
                try { parsed.price = new java.math.BigDecimal(p.replaceAll("[^0-9]", "")); } catch (Exception ignore) {}
            }
            JsonNode imageNode = meta.path("image");
            if (imageNode.isArray()) {
                for (JsonNode img : imageNode) {
                    String u = img.asText(""); if (!u.isBlank()) parsed.images.add(u);
                }
            }
        } catch (Exception ignore) {}
    }

    private void fillFallbackMeta(String html, ParsedProduct parsed) {
        if (parsed.name == null || parsed.name.isBlank()) parsed.name = extractMeta(html, "og:title");
        if (parsed.brand == null || parsed.brand.isBlank()) parsed.brand = extractMeta(html, "product:brand");
        if (parsed.price.compareTo(BigDecimal.ZERO) == 0) {
            String p = extractMeta(html, "product:price:amount");
            if (p != null) {
                try { parsed.price = new BigDecimal(p.replaceAll("[^0-9]", "")); } catch (Exception ignore) {}
            }
        }
        if (parsed.images.isEmpty()) {
            String ogImg = extractMeta(html, "og:image");
            if (ogImg == null || ogImg.isBlank()) ogImg = extractMeta(html, "og:image:url");
            if (ogImg == null || ogImg.isBlank()) ogImg = extractMeta(html, "og:image:secure_url");
            if (ogImg == null || ogImg.isBlank()) ogImg = extractMeta(html, "twitter:image");
            if (ogImg != null && !ogImg.isBlank() && ogImg.startsWith("http")) {
                parsed.images.add(ogImg.trim());
            }
        }
    }

    private ClothExtractionResponseDto buildFastResponse(String productUrl, ParsedProduct p) {
        if ((p.name != null && !p.name.isBlank()) || p.price.compareTo(BigDecimal.ZERO) > 0) {
            return ClothExtractionResponseDto.builder()
                    .productName(p.name != null ? p.name : "")
                    .brand((p.brand != null && !p.brand.isBlank()) ? p.brand : "알 수 없음")
                    .category("기타")
                    .colors(java.util.List.of("기본색상"))
                    .sizes(java.util.List.of("FREE"))
                    .material("소재 정보 없음")
                    .price(p.price)
                    .images(p.images)
                    .productUrl(productUrl)
                    .description("FAST HTML 파서에서 추출한 상품 정보입니다.")
                    .isAvailable(true)
                    .build();
        }
        return null;
    }

    private static class ParsedProduct {
        String name;
        String brand;
        BigDecimal price = BigDecimal.ZERO;
        List<String> images = new ArrayList<>();
    }

    private static String getString(String html) {
        String ld = null;
        int idx = html.indexOf("application/ld+json");
        if (idx > -1) {
            int start = html.lastIndexOf('<', idx);
            int end = html.indexOf("</script>", idx);
            if (start > -1 && end > idx) {
                String tag = html.substring(start, end);
                int gt = tag.indexOf('>');
                if (gt > -1 && gt + 1 < tag.length()) {
                    ld = tag.substring(gt + 1);
                }
            }
        }
        return ld;
    }

    private String extractMeta(String html, String property) {
        try {
            Matcher m = Pattern.compile("<meta[^>]*property\\s*=\\s*\"" + Pattern.quote(property) +
                    "\"[^>]*content\\s*=\\s*\"([^\"]+)\"", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(html);
            if (m.find()) return m.group(1);
        } catch (Exception ignore) {}
        return null;
    }
}



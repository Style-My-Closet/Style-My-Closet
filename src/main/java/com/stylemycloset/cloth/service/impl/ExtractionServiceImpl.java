package com.stylemycloset.cloth.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stylemycloset.binarycontent.entity.BinaryContent;
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

    @Override
    public ClothResponseDto extractAndSave(String productUrl, Long userId) {
        // 1) 빠른 HTML 파싱으로 최소 정보 확보
        ClothExtractionResponseDto extracted = extractMinimalInfo(productUrl);

        // 2) 이미지(있으면 1장) 선저장
        java.util.List<BinaryContent> images = downloadFirstImage(extracted);

        // 3) 의류 생성 요청 DTO 구성 및 저장
        ClothCreateRequestDto createRequest = buildCreateRequest(extracted, images);
        ClothResponseDto created = clothService.createCloth(createRequest, userId);

        // 4) AI 분석으로 속성 저장 (data URI 또는 외부 URL 우선순위)
        String imageUrl = chooseImageUrl(images, extracted);
        created = analyzeAndUpsertAttributes(created, imageUrl);

        return created;
    }

    private ClothExtractionResponseDto extractMinimalInfo(String productUrl) {
        ClothExtractionResponseDto ex = fastExtractFromHtml(productUrl);
        return (ex != null) ? ex : ClothExtractionResponseDto.createFailureResponse(productUrl);
    }

    private List<BinaryContent> downloadFirstImage(ClothExtractionResponseDto ex) {
        if (ex == null || ex.getImages() == null || ex.getImages().isEmpty()) {
            return List.of();
        }
        try {
            return imageDownloadService.downloadAndSaveImages(List.of(ex.getImages().getFirst()));
        } catch (Exception ignore) {
            return List.of();
        }
    }

    private ClothCreateRequestDto buildCreateRequest(ClothExtractionResponseDto ex, List<BinaryContent> images) {
        ClothCreateRequestDto req = new ClothCreateRequestDto();
        req.setName(ex != null ? ex.getProductName() : null);
        req.setType(ex != null ? ex.getCategory() : null);
        if (images != null && !images.isEmpty()) {
            req.setBinaryContentId(images.getFirst().getId());
        }
        return req;
    }

    private String chooseImageUrl(List<BinaryContent> images, ClothExtractionResponseDto ex) {
        try {
            if (images != null && !images.isEmpty() && images.getFirst().getImageUrl() != null) {
                return images.getFirst().getImageUrl();
            }
            if (ex != null && ex.getImages() != null && !ex.getImages().isEmpty() && ex.getImages().getFirst().startsWith("http")) {
                return ex.getImages().getFirst();
            }
        } catch (Exception ignore) {}
        return null;
    }

    private ClothResponseDto analyzeAndUpsertAttributes(ClothResponseDto created, String imageUrl) {
        if (imageUrl == null) return created;
        try {
            String json = imageVisionService.analyzeImageToJson(imageUrl);
            if (json == null || json.contains("AI_DISABLED") || json.contains("AI_HTTP_ERROR")) {
                return created;
            }
            var root = ImageAiParser.extractContentJson(json);
            var attrs = ImageAiParser.mapAttributes(root);
            Long clothId = Long.valueOf(created.getId());
            clothService.upsertAttributesByName(clothId, attrs);
            return clothService.getClothResponseById(clothId);
        } catch (Exception e) {
            return created;
        }
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



package com.stylemycloset.clothes.service.extract.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stylemycloset.clothes.dto.clothes.ClothesDto;
import com.stylemycloset.clothes.dto.extract.ClothExtractionResponseDto;
import com.stylemycloset.clothes.service.extract.ClothesProductExtractionService;
import com.stylemycloset.clothes.service.extract.parser.ImageAiParser;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Slf4j
@Service
@RequiredArgsConstructor
public class ExtractionServiceImpl implements ClothesProductExtractionService {

  private final RestTemplate restTemplate;
  private final ImageVisionService imageVisionService;

  @Override
  public ClothesDto extractImageAndBasicInfo(String url, Long userId) {
    // 1) 빠른 HTML 파싱으로 최소 정보 확보
    ClothExtractionResponseDto extracted = extractMinimalInfo(url);

    // 3) 병렬 처리: AI 분석 + 이미지 저장
    String originalImageUrl = getOriginalImageUrl(extracted);
    if (originalImageUrl == null) {
      return null; // 아니면 예외 던지기
    }

    // TODO: 8/19/25 이미지 url을 반환합니다
    // 3-1) AI 분석, 뭘 분석 하는거지?
    // 이미지를 가지고 분석을하는건가?
    // 이미지를 가지고 어떻게 분석을해
    // 최소한의 정보는 넘겨줘야지
    CompletableFuture<Map<String, List<String>>> aiAnalysis = CompletableFuture.supplyAsync(
        () -> {
          try {
            String json = imageVisionService.analyzeImageToJson(originalImageUrl);
            if (json != null && !json.contains("AI_DISABLED") && !json.contains(
                "AI_HTTP_ERROR")) {
              JsonNode root = ImageAiParser.extractContentJson(json);
              return ImageAiParser.mapAttributes(root);
            }
          } catch (Exception e) {
            log.debug("AI 분석 실패: {}", e.getMessage());
          }

          return null;
        });

    return null;
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

  // 최소한의 정보만 받아옵니다.
  private ClothExtractionResponseDto extractMinimalInfo(String productUrl) {
    ClothExtractionResponseDto ex = fastExtractFromHtml(productUrl);
    if (ex != null) {
      return ex;
    } else {
      return ClothExtractionResponseDto.createFailureResponse(productUrl);
    }
  }

  private ClothExtractionResponseDto fastExtractFromHtml(String productUrl) {
    try {
      String html = restTemplate.getForObject(productUrl, String.class);
      if (html == null || html.isBlank()) {
        return null;
      }

      ParsedProduct parsed = new ParsedProduct();
      String ld = getString(html);
      try {
        JsonNode meta = new ObjectMapper().readTree(ld.trim());
        parsed.name = meta.path("definitionName").asText("");
        parsed.brand = meta.path("brand").path("definitionName").asText("");
        String p = meta.path("offers").path("price").asText("");
        if (p != null) {
          try {
            parsed.price = new java.math.BigDecimal(p.replaceAll("[^0-9]", ""));
          } catch (Exception ignore) {
          }
        }
        JsonNode imageNode = meta.path("image");
        if (imageNode.isArray()) {
          for (JsonNode img : imageNode) {
            String u = img.asText(""); // 이건 뭔데 ?
            if (!u.isBlank()) {
              parsed.images.add(u);
            }
          }
        }
      } catch (Exception ignore) {
      }

      if (parsed.name == null || parsed.name.isBlank()) {
        parsed.name = extractMeta(html, "og:title");
      }
      if (parsed.brand == null || parsed.brand.isBlank()) {
        parsed.brand = extractMeta(html, "product:brand");
      }
      if (parsed.price.compareTo(BigDecimal.ZERO) == 0) {
        String p = extractMeta(html, "product:price:amount");
        if (p != null) {
          try {
            parsed.price = new BigDecimal(p.replaceAll("[^0-9]", ""));
          } catch (Exception ignore) {
          }
        }
      }
      if (parsed.images.isEmpty()) {
        String ogImg = extractMeta(html, "og:image");
        if (ogImg == null || ogImg.isBlank()) {
          ogImg = extractMeta(html, "og:image:url");
        }
        if (ogImg == null || ogImg.isBlank()) {
          ogImg = extractMeta(html, "og:image:secure_url"); // 이건 필요해?
        }
        if (ogImg == null || ogImg.isBlank()) {
          ogImg = extractMeta(html, "twitter:image");
        }
        if (ogImg != null && !ogImg.isBlank() && ogImg.startsWith("http")) {
          parsed.images.add(ogImg.trim());
        }
      }

      return buildFastResponse(productUrl, parsed);
    } catch (Exception e) {
      return null;
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
    Matcher m = Pattern.compile("<meta[^>]*property\\s*=\\s*\"" + Pattern.quote(property) +
            "\"[^>]*content\\s*=\\s*\"([^\"]+)\"", java.util.regex.Pattern.CASE_INSENSITIVE)
        .matcher(html);
    if (m.find()) {
      return m.group(1);
    }

    throw new IllegalArgumentException("파싱 에러 발생");
  }

}



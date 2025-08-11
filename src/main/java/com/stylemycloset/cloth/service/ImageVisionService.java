package com.stylemycloset.cloth.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import java.util.Base64;

@Service
@Slf4j
public class ImageVisionService {
    private static final int MAX_IMAGE_DIMENSION = 500;
    @Autowired(required = false)
    @Nullable
    private RestTemplate restTemplate;

    @Value("${OPENAI_API_KEY:${CUSTOM_AI_AUTH_VALUE:}}")
    private String openAiApiKey;

    @Value("${OPENAI_MODEL:gpt-5-nano-2025-08-07}")
    private String openAiModel;

    @Value("${OPENAI_ORG_ID:}")
    private String openAiOrgId;

    @Value("${OPENAI_PROJECT_ID:}")
    private String openAiProjectId;

    /**
     * Calls OpenAI Responses API with an image (URL or data URI) and asks for structured JSON
     * describing fashion product attributes. Returns the raw API response body as String.
     */
    public String analyzeImageToJson(String imageUrl) {
        if (openAiApiKey == null || openAiApiKey.isBlank()) {
            return "{\"error\":\"AI_DISABLED\"}";
        }
        RestTemplate httpClient = (this.restTemplate != null) ? this.restTemplate : new RestTemplate();
        try {
            String endpointUrl = "https://api.openai.com/v1/responses";
            HttpHeaders headers = createHeaders();
            ObjectMapper mapper = new ObjectMapper();
            String payloadJson = buildPayloadJson(mapper, imageUrl);

            HttpEntity<String> httpEntity = new HttpEntity<>(payloadJson, headers);
            ResponseEntity<String> res = httpClient.postForEntity(endpointUrl, httpEntity, String.class);
            try {
                String requestId = res.getHeaders().getFirst("x-request-id");
                if (requestId != null) {
                    log.info("OpenAI x-request-id={}", requestId);
                }
            } catch (Exception ignore) {}
            String body = res.getBody();
            // 구조화 응답은 output_parsed에 위치하므로 전체 바디를 반환하고, 호출부에서 output_parsed를 우선 파싱
            return body;
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            String body = e.getResponseBodyAsString();
            log.warn("OpenAI 4xx: status={}, body={}", e.getStatusCode(), body);
            return body != null && !body.isBlank() ? body : "{\"error\":\"AI_HTTP_ERROR\",\"message\":\"" + e.getMessage().replace("\"","\\\"") + "\"}";
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            String body = e.getResponseBodyAsString();
            log.warn("OpenAI 5xx: status={}, body={}", e.getStatusCode(), body);
            return body != null && !body.isBlank() ? body : "{\"error\":\"AI_HTTP_ERROR\",\"message\":\"" + e.getMessage().replace("\"","\\\"") + "\"}";
        } catch (Exception e) {
            log.warn("OpenAI call error: {}", e.getMessage());
            return "{\"error\":\"AI_HTTP_ERROR\",\"message\":\"" + e.getMessage().replace("\"","\\\"") + "\"}";
        }
    }


    public String analyzeImageBytesToJson(byte[] imageBytes, String contentType) {
        if (imageBytes == null || imageBytes.length == 0) {
            return "{\"error\":\"EMPTY_IMAGE\"}";
        }
        String safeContentType = (contentType == null || contentType.isBlank()) ? "image/jpeg" : contentType.trim();
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        String dataUri = "data:" + safeContentType + ";base64," + base64;
        return analyzeImageToJson(dataUri);
    }


    /**
     * If the input is a data URI, downscale it up to MAX_IMAGE_DIMENSION to reduce payload size.
     * External URLs are returned as-is.
     */
    private String ensureThumbnailDataUri(String imageUrlOrDataUri) {
        try {
            if (imageUrlOrDataUri == null) return null;
            if (!imageUrlOrDataUri.startsWith("data:")) {
                return imageUrlOrDataUri; // 외부 URL은 그대로
            }
            int comma = imageUrlOrDataUri.indexOf(',');
            if (comma <= 0) return imageUrlOrDataUri;
            String meta = imageUrlOrDataUri.substring(5, comma); // e.g. image/jpeg;base64
            String b64 = imageUrlOrDataUri.substring(comma + 1);
            byte[] bytes = java.util.Base64.getDecoder().decode(b64);

            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(bytes);
            java.awt.image.BufferedImage src = javax.imageio.ImageIO.read(bais);
            if (src == null) return imageUrlOrDataUri;
            int w = src.getWidth();
            int h = src.getHeight();
            if (w <= MAX_IMAGE_DIMENSION && h <= MAX_IMAGE_DIMENSION) return imageUrlOrDataUri;
            double scale = Math.min((double) MAX_IMAGE_DIMENSION / w, (double) MAX_IMAGE_DIMENSION / h);
            int nw = Math.max(1, (int) Math.round(w * scale));
            int nh = Math.max(1, (int) Math.round(h * scale));
            java.awt.image.BufferedImage dst = new java.awt.image.BufferedImage(nw, nh, java.awt.image.BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = dst.createGraphics();
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(src, 0, 0, nw, nh, null);
            g.dispose();
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            String fmt = meta.contains("png") ? "png" : "jpg";
            javax.imageio.ImageIO.write(dst, fmt, baos);
            String nb64 = java.util.Base64.getEncoder().encodeToString(baos.toByteArray());
            return "data:" + meta + "," + nb64;
        } catch (Exception e) {
            return imageUrlOrDataUri;
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);
        if (openAiOrgId != null && !openAiOrgId.isBlank()) {
            headers.set("OpenAI-Organization", openAiOrgId.trim());
        }
        if (openAiProjectId != null && !openAiProjectId.isBlank()) {
            headers.set("OpenAI-Project", openAiProjectId.trim());
        }
        return headers;
    }

    private String buildPayloadJson(ObjectMapper mapper, String imageUrl) throws Exception {
        JsonNode textFormat = mapper.readTree("""
                {
                  "format": {
                    "type": "json_schema",
                    "name": "FashionProductAttributes",
                    "strict": false,
                    "schema": {
                      "type": "object",
                      "additionalProperties": false,
                      "properties": {
                        "category": { "type": "string" },
                        "primary_color": { "type": "string" },
                        "pattern": { "type": ["string", "null"] },
                        "material": { "type": ["string", "null"] },
                        "fit_type": { "type": "string" },
                        "sleeve_type": { "type": "string" },
                        "neckline_type": { "type": "string" },
                        "shoe_type": { "type": ["string","null"] },
                        "closure_type": { "type": ["string","null"] },
                        "heel_height_cm": { "type": ["number","null"] },
                        "bag_type": { "type": ["string","null"] },
                        "strap_type": { "type": ["string","null"] },
                        "capacity_liters": { "type": ["number","null"] },
                        "confidence": { "type": "number" }
                      },
                      "required": ["category","primary_color","confidence"]
                    }
                  }
                }""");

        ObjectNode payloadNode = mapper.createObjectNode().put("model", openAiModel);
        payloadNode.set("text", textFormat);

        com.fasterxml.jackson.databind.node.ArrayNode inputArr = mapper.createArrayNode();
        com.fasterxml.jackson.databind.node.ObjectNode userObj = mapper.createObjectNode();
        userObj.put("role", "user");
        com.fasterxml.jackson.databind.node.ArrayNode contentArr = mapper.createArrayNode();
        contentArr.add(mapper.createObjectNode()
                .put("type", "input_text")
                .put("text", "패션 제품(의류/신발/가방/액세서리)의 속성만 JSON으로 반환하세요. 반드시 category, primary_color, fit_type, sleeve_type, neckline_type, confidence는 채우고, 확신이 낮아도 근거 기반으로 추정값을 입력하세요. 나머지 필드는 알 수 없으면 null로 두세요. 오직 text.format 스키마에 맞춘 JSON만 출력하세요. 추가 텍스트 금지."));

        com.fasterxml.jackson.databind.node.ObjectNode imgNode = mapper.createObjectNode();
        imgNode.put("type", "input_image");
        imgNode.put("image_url", ensureThumbnailDataUri(imageUrl));
        contentArr.add(imgNode);

        userObj.set("content", contentArr);
        inputArr.add(userObj);
        payloadNode.set("input", inputArr);
        return mapper.writeValueAsString(payloadNode);
    }


}



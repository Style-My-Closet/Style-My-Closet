package com.stylemycloset.cloth.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.*;


@Slf4j
public final class ImageAiParser {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ImageAiParser() {}

    public static JsonNode extractContentJson(String rawResponse) {
        if (isBlank(rawResponse)) {
            return null;
        }

        try {
            JsonNode rootNode = OBJECT_MAPPER.readTree(rawResponse.trim());
            return findContentFromResponse(rootNode);
        } catch (Exception e) {
            log.warn("AI 응답 파싱 실패: {}", e.getMessage());
            return null;
        }
    }


    public static Map<String, List<String>> mapAttributes(JsonNode contentNode) {
        Map<String, List<String>> attributes = new HashMap<>();
        
        if (contentNode == null || !contentNode.isObject()) {
            return attributes;
        }


        extractBasicAttributes(contentNode, attributes);
        

        extractNumericAttributes(contentNode, attributes);

        return attributes;
    }

    private static JsonNode findContentFromResponse(JsonNode responseNode) {

        if (hasClothingSchema(responseNode)) {
            return responseNode;
        }

        JsonNode parsed = responseNode.path("output_parsed");
        if (isValidContent(parsed)) {
            return parseJsonField(parsed);
        }

        JsonNode outputText = responseNode.path("output_text");
        if (outputText.isTextual()) {
            return parseTextAsJson(outputText.asText());
        }

        JsonNode output = responseNode.path("output");
        if (output.isArray()) {
            JsonNode content = findContentInOutputArray(output);
            if (content != null) {
                return content;
            }
        }

        return responseNode;
    }

    private static boolean hasClothingSchema(JsonNode node) {
        return node.has("category") || 
               node.has("primary_color") || 
               node.has("fit_type") ||
               node.has("material");
    }

    private static boolean isValidContent(JsonNode node) {
        return node != null && !node.isMissingNode() && !node.isNull();
    }

    private static JsonNode parseJsonField(JsonNode field) {
        if (field.isTextual()) {
            return parseTextAsJson(field.asText());
        } else if (field.isObject()) {
            return field;
        }
        return null;
    }

    private static JsonNode parseTextAsJson(String text) {
        if (isBlank(text)) {
            return null;
        }
        
        try {
            return OBJECT_MAPPER.readTree(text);
        } catch (Exception e) {
            log.debug("텍스트 JSON 파싱 실패: {}", e.getMessage());
            return null;
        }
    }

    private static JsonNode findContentInOutputArray(JsonNode outputArray) {
        for (JsonNode element : outputArray) {
            if (!"message".equals(element.path("type").asText())) {
                continue;
            }

            JsonNode contentArray = element.path("content");
            if (!contentArray.isArray()) {
                continue;
            }

            for (JsonNode content : contentArray) {
                JsonNode result = extractFromContentElement(content);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private static JsonNode extractFromContentElement(JsonNode contentElement) {
        if ("output_text".equals(contentElement.path("type").asText())) {
            String text = contentElement.path("text").asText();
            return parseTextAsJson(text);
        }

        JsonNode parsed = contentElement.path("parsed");
        if (isValidContent(parsed)) {
            return parseJsonField(parsed);
        }

        return null;
    }

    private static void extractBasicAttributes(JsonNode node, Map<String, List<String>> attributes) {
        addAttributeIfPresent(attributes, "색상", getTextValue(node, "primary_color"));
        addAttributeIfPresent(attributes, "소재", getMaterialValue(node));
        addAttributeIfPresent(attributes, "핏", normalizeFit(getTextValue(node, "fit_type")));
        addAttributeIfPresent(attributes, "소매", normalizeSleeve(getTextValue(node, "sleeve_type")));
        addAttributeIfPresent(attributes, "넥라인", normalizeNeckline(getTextValue(node, "neckline_type")));
        addAttributeIfPresent(attributes, "패턴", getTextValue(node, "pattern"));
        addAttributeIfPresent(attributes, "신발유형", getTextValue(node, "shoe_type"));
        addAttributeIfPresent(attributes, "클로저", getTextValue(node, "closure_type"));
        addAttributeIfPresent(attributes, "가방유형", getTextValue(node, "bag_type"));
    }


    private static void extractNumericAttributes(JsonNode node, Map<String, List<String>> attributes) {
        addAttributeIfPresent(attributes, "용량(L)", getNumericValue(node, "capacity_liters"));
        addAttributeIfPresent(attributes, "굽높이(cm)", getNumericValue(node, "heel_height_cm"));
    }


    private static String getMaterialValue(JsonNode node) {
        String material = getTextValue(node, "material");
        return material != null ? material : getTextValue(node, "fabric_type");
    }


    private static String getTextValue(JsonNode node, String fieldName) {
        return cleanText(node.path(fieldName).asText(null));
    }


    private static String getNumericValue(JsonNode node, String fieldName) {
        JsonNode field = node.path(fieldName);
        if (field.isNumber()) {
            return String.valueOf(field.asDouble());
        }
        return null;
    }


    private static void addAttributeIfPresent(Map<String, List<String>> attributes, String key, String value) {
        if (value != null && !value.isEmpty()) {
            attributes.put(key, List.of(value));
        }
    }


    private static String cleanText(String text) {
        if (text == null || text.trim().isEmpty() || "null".equalsIgnoreCase(text.trim())) {
            return null;
        }
        return text.trim();
    }


    private static boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }


    private static String normalizeFit(String fit) {
        if (fit == null) return null;
        
        String normalized = fit.toLowerCase(Locale.ROOT);
        if (normalized.contains("oversize")) return "oversized";
        if (normalized.contains("regular")) return "regular";
        if (normalized.contains("slim")) return "slim";
        if (normalized.contains("relax")) return "relaxed";
        return fit;
    }

    private static String normalizeSleeve(String sleeve) {
        if (sleeve == null) return null;
        
        String normalized = sleeve.toLowerCase(Locale.ROOT);
        if (normalized.contains("short")) return "short";
        if (normalized.contains("long")) return "long";
        if (normalized.contains("sleeveless")) return "sleeveless";
        if (normalized.contains("half")) return "half";
        return sleeve;
    }

    private static String normalizeNeckline(String neckline) {
        if (neckline == null) return null;
        
        String normalized = neckline.toLowerCase(Locale.ROOT);
        if (normalized.contains("crew") || normalized.contains("round")) return "round";
        if (normalized.contains("v")) return "vneck";
        if (normalized.contains("collar")) return "collar";
        if (normalized.contains("hood")) return "hood";
        return neckline;
    }
}

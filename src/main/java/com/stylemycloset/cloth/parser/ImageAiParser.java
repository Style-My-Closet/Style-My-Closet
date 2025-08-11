package com.stylemycloset.cloth.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;


public final class ImageAiParser {

    private ImageAiParser() {}


    public static JsonNode extractContentJson(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            ObjectMapper om = new ObjectMapper();
            // 1) 먼저 content JSON으로 바로 파싱 시도
            try {
                JsonNode maybeContent = om.readTree(raw.trim());
                // 의도 스키마(카테고리 등의 키)가 보이면 그대로 사용
                if (maybeContent.has("category") || maybeContent.has("primary_color") || maybeContent.has("fit_type")) {
                    return maybeContent;
                }
                // 1-1) Responses 구조화 출력: output_parsed 우선 사용
                JsonNode parsed = maybeContent.path("output_parsed");
                if (parsed != null && !parsed.isMissingNode() && !parsed.isNull()) {

                    if (parsed.isTextual()) {
                        try { return om.readTree(parsed.asText()); } catch (Exception ignore2) {}
                    } else if (parsed.isObject()) {
                        return parsed;
                    }
                }
                // 2) Responses 바디로 간주하고 경로 탐색
                String content = maybeContent.path("output_text").asText(null);
                if (content != null && !content.isBlank()) {
                    return om.readTree(content);
                }
                JsonNode outArr = maybeContent.path("output");
                if (outArr.isArray()) {
                    for (JsonNode elem : outArr) {
                        if ("message".equals(elem.path("type").asText())) {
                            JsonNode cArr = elem.path("content");
                            if (cArr.isArray()) {
                                for (JsonNode c : cArr) {
                                    if ("output_text".equals(c.path("type").asText())) {
                                        String t = c.path("text").asText(null);
                                        if (t != null && !t.isBlank()) {
                                            return om.readTree(t);
                                        }
                                    }
                                    // 일부 SDK는 parsed 키를 content 단위로 제공할 수 있음
                                    JsonNode parsed2 = c.path("parsed");
                                    if (parsed2 != null && !parsed2.isMissingNode() && !parsed2.isNull()) {
                                        if (parsed2.isTextual()) {
                                            try { return om.readTree(parsed2.asText()); } catch (Exception ignore3) {}
                                        } else if (parsed2.isObject()) {
                                            return parsed2;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                return maybeContent;
            } catch (Exception ignore) { }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static Map<String, List<String>> mapAttributes(JsonNode root) {
        Map<String, List<String>> attributes = new HashMap<>();
        if (root == null || !root.isObject()) return attributes;

        String color = nonBlank(root.path("primary_color").asText(null));
        if (color != null) attributes.put("색상", List.of(color));

        String material = nonBlank(root.path("material").asText(null));
        if (material == null) material = nonBlank(root.path("fabric_type").asText(null));
        if (material != null) attributes.put("소재", List.of(material));

        String fit = nonBlank(root.path("fit_type").asText(null));
        if (fit != null) attributes.put("핏", List.of(normalizeFit(fit)));

        String sleeve = nonBlank(root.path("sleeve_type").asText(null));
        if (sleeve != null) attributes.put("소매", List.of(normalizeSleeve(sleeve)));

        String neckline = nonBlank(root.path("neckline_type").asText(null));
        if (neckline != null) attributes.put("넥라인", List.of(normalizeNeckline(neckline)));

        String pattern = nonBlank(root.path("pattern").asText(null));
        if (pattern != null) attributes.put("패턴", List.of(pattern));

        String shoeType = nonBlank(root.path("shoe_type").asText(null));
        if (shoeType != null) attributes.put("신발유형", List.of(shoeType));

        String closure = nonBlank(root.path("closure_type").asText(null));
        if (closure != null) attributes.put("클로저", List.of(closure));

        String bagType = nonBlank(root.path("bag_type").asText(null));
        if (bagType != null) attributes.put("가방유형", List.of(bagType));

        String capacity = root.path("capacity_liters").isNumber() ? String.valueOf(root.path("capacity_liters").asDouble()) : null;
        if (nonBlank(capacity) != null) attributes.put("용량(L)", List.of(capacity));

        String heel = root.path("heel_height_cm").isNumber() ? String.valueOf(root.path("heel_height_cm").asDouble()) : null;
        if (nonBlank(heel) != null) attributes.put("굽높이(cm)", List.of(heel));

        return attributes;
    }

    private static String nonBlank(String s) {
        return (s != null && !s.trim().isEmpty() && !"null".equalsIgnoreCase(s.trim())) ? s.trim() : null;
    }

    private static String normalizeFit(String fit) {
        String f = fit.toLowerCase(Locale.ROOT);
        if (f.contains("oversize")) return "oversized";
        if (f.contains("regular")) return "regular";
        if (f.contains("slim")) return "slim";
        if (f.contains("relax")) return "relaxed";
        return fit;
    }

    private static String normalizeSleeve(String sleeve) {
        String s = sleeve.toLowerCase(Locale.ROOT);
        if (s.contains("short")) return "short";
        if (s.contains("long")) return "long";
        if (s.contains("sleeveless")) return "sleeveless";
        if (s.contains("half")) return "half";
        return sleeve;
    }

    private static String normalizeNeckline(String neckline) {
        String n = neckline.toLowerCase(Locale.ROOT);
        if (n.contains("crew") || n.contains("round")) return "round";
        if (n.contains("v")) return "vneck";
        if (n.contains("collar")) return "collar";
        if (n.contains("hood")) return "hood";
        return neckline;
    }
}



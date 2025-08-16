package com.stylemycloset.weather.util;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class WeatherItemsFilterer {

    private final Set<String> seenKeys = new HashSet<>();
    private final Set<String> AVAILABLE_CATEGORIES = Set.of("TMP", "TMN", "TMX", "POP", "PTY", "PCP","REH","WSD");

    public List<JsonNode> filtering(List<JsonNode> items) {
        List<JsonNode> result = new ArrayList<>();

        for (JsonNode item : items) {
            String key = createUniqueKey(item);
            if (seenKeys.add(key) && dataCleaning(item)) {
                result.add(item);
            }
        }
        return result;
    }

    private Boolean dataCleaning(JsonNode item) {
        String category = item.path("category").asText();
        String fcstTime = item.path("fcstTime").asText();

        // 1) 카테고리가 지원 목록에 있어야 함
        if (!AVAILABLE_CATEGORIES.contains(category)) {
            return false;
        }

        // 2) TMN, TMX는 무조건 통과
        if (category.equals("TMN") || category.equals("TMX")) {
            return true;
        }

        // 3) 나머지는 fcstTime == 2300 일 때만 통과
        return "2300".equals(fcstTime);
    }

    private String createUniqueKey(JsonNode item) {
        return item.path("baseDate").asText() + "|" +
            item.path("baseTime").asText() + "|" +
            item.path("fcstDate").asText() + "|" +
            item.path("fcstTime").asText() + "|" +
            item.path("nx").asText() + "," + item.path("ny").asText() + "|" +
            item.path("category").asText();
    }
}


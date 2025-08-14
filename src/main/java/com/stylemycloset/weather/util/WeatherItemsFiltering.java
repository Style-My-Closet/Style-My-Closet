package com.stylemycloset.weather.util;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class WeatherItemsFiltering {

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
        return AVAILABLE_CATEGORIES.contains(item.path("category").asText());
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


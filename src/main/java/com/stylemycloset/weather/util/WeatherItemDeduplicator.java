package com.stylemycloset.weather.util;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class WeatherItemDeduplicator {

    private final Set<String> seenKeys = new HashSet<>();

    public List<JsonNode> deduplicate(List<JsonNode> items) {
        List<JsonNode> result = new ArrayList<>();

        for (JsonNode item : items) {
            String key = createUniqueKey(item);
            if (seenKeys.add(key)) {
                result.add(item);
            }
        }
        return result;
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


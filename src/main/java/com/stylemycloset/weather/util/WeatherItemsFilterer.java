package com.stylemycloset.weather.util;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class WeatherItemsFilterer {

    protected final Set<String> seenKeys = new HashSet<>();
    private final Set<String> AVAILABLE_CATEGORIES = Set.of("TMP", "TMN", "TMX", "POP", "PTY", "PCP","REH","WSD");

    public Boolean dataCleaning(JsonNode item,String baseDate,String baseTime) {
        String category = item.path("category").asText();
        String fcstTime = item.path("fcstTime").asText();
        String fcstDate = item.path("fcstDate").asText();

        // 1) 카테고리가 지원 목록에 있어야 함
        if (!AVAILABLE_CATEGORIES.contains(category)) {
            return false;
        }

        // 2) TMN, TMX는 무조건 통과
        if (category.equals("TMN") || category.equals("TMX")) {
            return true;
        }

        // 3) 나머지는 fcstTime == 2300 일 때만 통과
        if(fcstDate.equals(baseDate)) {
            return DateTimeUtils.allowedBaseTime(fcstTime).equals(baseTime);
        }else return "0200".equals(fcstTime);

    }

    protected String createUniqueKey(JsonNode item) {
        return item.path("baseDate").asText() + "|" +
            item.path("baseTime").asText() + "|" +
            item.path("fcstDate").asText() + "|" +
            item.path("fcstTime").asText() + "|" +
            item.path("nx").asText() + "," + item.path("ny").asText() + "|" +
            item.path("category").asText();
    }
}


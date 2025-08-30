package com.stylemycloset.weather.util;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class WeatherItemsFilterer {

    protected final Set<String> seenKeys = new HashSet<>();
    private final Set<String> AVAILABLE_CATEGORIES = Set.of("TMP", "TMN", "TMX", "POP", "PTY", "PCP","REH","WSD","SKY");

    public Boolean dataCleaning(JsonNode item,String baseDate) {
        String category = item.path("category").asText();
        String fcstTime = item.path("fcstTime").asText();
        String fcstDate = item.path("fcstDate").asText();

        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        String hh00 = String.format("%02d00", hour);


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
            return fcstTime.equals(hh00);
        }else return "0000".equals(fcstTime);

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


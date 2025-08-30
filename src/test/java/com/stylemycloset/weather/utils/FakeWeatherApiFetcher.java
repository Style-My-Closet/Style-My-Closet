package com.stylemycloset.weather.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stylemycloset.location.Location;
import com.stylemycloset.weather.util.WeatherApiFetcher;
import com.stylemycloset.weather.util.WeatherItemsFilterer;
import java.util.List;


import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@Profile("test")// 테스트 환경에서만 빈 등록되도록 profile 지정 가능
public class FakeWeatherApiFetcher extends WeatherApiFetcher {

    private final ObjectMapper objectMapper;

    public FakeWeatherApiFetcher(ObjectMapper objectMapper, WeatherItemsFilterer weatherItemsFilterer) {
        super(null, objectMapper, weatherItemsFilterer); // 실제 RestTemplate은 사용 안 함
        this.objectMapper = objectMapper;
    }

    @Override
    public List<JsonNode> fetchAllPages(String baseDate, String baseTime, Location location) {
        List<JsonNode> fakeItems = new ArrayList<>();
        try {
            JsonNode tmpNode = objectMapper.readTree("""
                {
                    "baseDate": "%s",
                    "baseTime": "%s",
                    "category": "TMP",
                    "fcstDate": "%s",
                    "fcstTime": "0300",
                    "fcstValue": "25.4",
                    "nx": %d,
                    "ny": %d
                }
                """.formatted(baseDate, baseTime, baseDate, location.getX(), location.getY()));

            JsonNode rehNode = objectMapper.readTree("""
                {
                    "baseDate": "%s",
                    "baseTime": "%s",
                    "category": "REH",
                    "fcstDate": "%s",
                    "fcstTime": "0300",
                    "fcstValue": "80",
                    "nx": %d,
                    "ny": %d
                }
                """.formatted(baseDate, baseTime, baseDate, location.getX(), location.getY()));

            fakeItems.add(tmpNode);
            fakeItems.add(rehNode);

        } catch (Exception e) {
            throw new RuntimeException("FakeWeatherApiFetcher 데이터 생성 실패", e);
        }

        return fakeItems;
    }
}

package com.stylemycloset.weather.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import com.stylemycloset.location.Location;
import com.stylemycloset.weather.entity.Humidity;
import com.stylemycloset.weather.entity.Precipitation;
import com.stylemycloset.weather.entity.Temperature;
import com.stylemycloset.weather.entity.Weather;
import com.stylemycloset.weather.entity.WindSpeed;
import com.stylemycloset.weather.repository.WeatherRepository;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class openApiService {
    private final WeatherRepository weatherRepository;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${external.market-index.service-key}")
    private String serviceKey;

    @Value("${external.market-index.base-url}")
    private String baseUrl;

    @Value("${external.market-index.num-of-rows:100}")
    private int numOfRows;

    public void fetchData(String baseDate, String baseTime, Location location) {
        Set<String> seenKeys = new HashSet<>();
        int totalPages = (int) Math.ceil((double) 100 / numOfRows); // TODO: 총 개수 동적으로 바꾸기

        List<JsonNode> allItems = new ArrayList<>();

        for (int page = 1; page <= totalPages; page++) {
            try {
                String apiUrl = String.format("%s/getVilageFcst?serviceKey=%s&dataType=JSON&numOfRows=%d&pageNo=%d&"
                        + "base_date=%s&base_time=%s&nx=%d&ny=%d",
                    baseUrl, serviceKey, page, numOfRows, baseDate, baseTime, location.getX(), location.getY());
                URI uri = new URI(apiUrl);

                log.debug("[API 호출] 페이지 {}: {}", page, apiUrl);

                String response = restTemplate.getForObject(uri, String.class);

                JsonNode itemsNode = objectMapper.readTree(response)
                    .path("response").path("body").path("items").path("item");

                if (itemsNode.isMissingNode() || itemsNode.isNull()) {
                    log.warn("[경고] 페이지 {}에 데이터 없음", page);
                    continue;
                }

                if (itemsNode.isArray()) {
                    for (JsonNode item : itemsNode) {
                        String key = createUniqueKey(item);
                        if (!seenKeys.add(key)) {
                            log.debug("[중복 스킵] {}", key);
                            continue;
                        }
                        allItems.add(item);
                    }
                } else {
                    String key = createUniqueKey(itemsNode);
                    if (seenKeys.add(key)) {
                        allItems.add(itemsNode);
                    }
                }

                log.info("[성공] 페이지 {} 처리 완료", page);

            } catch (Exception e) {
                log.error("[실패] 페이지 {} 처리 중 예외 발생: {}", page, e.getMessage(), e);
                throw new StyleMyClosetException(ErrorCode.ERROR_CODE, Map.of("apiError", "api 호출 오류"));
            }
        }

        // 중복 제거된 모든 items 를 한 번에 처리
        processItems(allItems, baseDate, baseTime, location);
    }

    private String createUniqueKey(JsonNode item) {
        // 중복 체크용 키 생성: baseDate|baseTime|fcstDate|fcstTime|nx,ny|category
        return item.path("baseDate").asText() + "|" +
            item.path("baseTime").asText() + "|" +
            item.path("fcstDate").asText() + "|" +
            item.path("fcstTime").asText() + "|" +
            item.path("nx").asText() + "," + item.path("ny").asText() + "|" +
            item.path("category").asText();
    }

    private void processItems(List<JsonNode> items, String baseDate, String baseTime, Location location) {
        // 키: fcstDate + fcstTime + nx + ny  (예보 시각+좌표) - 이걸 기준으로 하나의 Weather 객체 생성
        Map<String, WeatherBuilderHelper> weatherBuilders = new HashMap<>();

        for (JsonNode item : items) {
            if (!item.path("baseDate").asText().equals(baseDate)) {
                log.debug("[스킵] 기준일 불일치: {}", createUniqueKey(item));
                continue;
            }

            String fcstDate = item.path("fcstDate").asText();
            String fcstTime = item.path("fcstTime").asText();
            String nx = item.path("nx").asText();
            String ny = item.path("ny").asText();

            String weatherKey = fcstDate + "|" + fcstTime + "|" + nx + "," + ny;

            WeatherBuilderHelper builder = weatherBuilders.computeIfAbsent(weatherKey,
                k -> new WeatherBuilderHelper(baseDate, baseTime, fcstDate, fcstTime, location));

            String category = item.path("category").asText();
            String fcstValue = item.path("fcstValue").asText();

            // 카테고리별로 builder에 값 세팅
            builder.setCategoryValue(category, fcstValue);
        }

        // 모든 builder 완성 후 Weather 객체 생성 및 저장 로직 추가 가능
        for (WeatherBuilderHelper builder : weatherBuilders.values()) {
            Weather weather = builder.build();
            // 저장 또는 후처리
            log.info("빌드된 Weather: {}", weather);
        }
    }
}

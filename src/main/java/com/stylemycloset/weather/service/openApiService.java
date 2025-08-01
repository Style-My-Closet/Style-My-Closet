package com.stylemycloset.weather.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import com.stylemycloset.location.Location;
import com.stylemycloset.weather.repository.WeatherRepository;
import java.net.URI;
import java.util.HashSet;
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

    public void fetchData(String baseDate, String baseTime, Location location){
        Set<String> seenKeys = new HashSet<>();
        int totalPages = (int) Math.ceil((double) 100 / numOfRows); // TODO: 총 개수 동적으로 바꾸기

        for (int page = 1; page <= totalPages; page++) {
            try {
                String apiUrl = String.format("%s/getVilageFcst?serviceKey=%s&numOfRows=%d&pageNo=%d&"
                        + "base_date=%s&base_time=%s&nx=%d&ny=%d",
                    baseUrl, serviceKey, page, numOfRows,baseDate,baseTime,location.getX(),location.getY());
                URI uri = new URI(apiUrl);

                log.debug("[API 호출] 페이지 {}: {}", page, apiUrl);

                String response = restTemplate.getForObject(uri, String.class);

                JsonNode items = objectMapper.readTree(response)
                    .path("response").path("body").path("items").path("item");

                if (items.isMissingNode() || items.isNull()) {
                    log.warn("[경고] 페이지 {}에 데이터 없음", page);
                    continue;
                }

                if (items.isArray()) {
                    for (JsonNode item : items) {
                        processItem(item, seenKeys, baseDate);
                    }
                } else {
                    processItem(items, seenKeys, baseDate);
                }

                log.info("[성공] 페이지 {} 처리 완료", page);

            } catch (Exception e) {
                log.error("[실패] 페이지 {} 처리 중 예외 발생: {}", page, e.getMessage(), e);
                throw new StyleMyClosetException(ErrorCode.ERROR_CODE, Map.of("apiError", "api 호출 오류"));
            }
        }
    }

    private void processItem(JsonNode item, Set<String> seenKeys,String baseDate) throws Exception {
        String indexClassification = item.path("idxCsf").asText();
        String indexName = item.path("idxNm").asText();
        String itemDate = item.path("basDt").asText();
        String key = indexClassification + "|" + indexName;

        if (!seenKeys.add(key)) {
            log.debug("[중복 스킵] {}", key);
            return;
        }

        if (!itemDate.equals(baseDate)) {
            log.debug("[스킵] 기준일 불일치: {}", key);
            return;
        }
    }
}

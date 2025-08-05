package com.stylemycloset.weather.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import com.stylemycloset.location.Location;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class WeatherApiFetcher {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${external.market-index.service-key}")
    private String serviceKey;

    @Value("${external.market-index.base-url}")
    private String baseUrl;

    @Value("${external.market-index.num-of-rows:100}")
    private int numOfRows;

    public List<JsonNode> fetchAllPages(String baseDate, String baseTime, Location location) {
        List<JsonNode> allItems = new ArrayList<>();
        int totalPages = (int) Math.ceil((double) 100 / numOfRows); // TODO: 총 개수 동적으로

        for (int page = 1; page <= totalPages; page++) {
            try {
                String apiUrl = String.format("%s/getVilageFcst?serviceKey=%s&dataType=JSON&numOfRows=%d&pageNo=%d&"
                        + "base_date=%s&base_time=%s&nx=%d&ny=%d",
                    baseUrl, serviceKey, numOfRows, page, baseDate, baseTime, location.getX(), location.getY());
                URI uri = new URI(apiUrl);

                String response = restTemplate.getForObject(uri, String.class);
                JsonNode itemsNode = objectMapper.readTree(response)
                    .path("response").path("body").path("items").path("item");

                if (itemsNode.isMissingNode() || itemsNode.isNull()) continue;

                if (itemsNode.isArray()) {
                    itemsNode.forEach(allItems::add);
                } else {
                    allItems.add(itemsNode);
                }

            } catch (Exception e) {
                throw new StyleMyClosetException(
                    ErrorCode.ERROR_CODE, Map.of("apiError", "API 호출 오류"));
            }
        }

        return allItems;
    }
}


package com.stylemycloset.weather.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.stylemycloset.location.Location;
import com.stylemycloset.weather.entity.Weather;
import com.stylemycloset.weather.processor.WeatherCategoryProcessor;
import com.stylemycloset.weather.util.DateTimeUtils;
import com.stylemycloset.weather.util.WeatherApiFetcher;
import com.stylemycloset.weather.util.WeatherBuilderHelper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ForecastApiService {

  private final WeatherApiFetcher apiFetcher;
  private final List<WeatherCategoryProcessor> processors;

  public List<Weather> fetchData(Location location) {

    LocalDateTime now = LocalDateTime.now();
    List<String> forecastTime = DateTimeUtils.toBaseDateAndTime(now);
    String baseDate = forecastTime.get(0);
    String baseTime = forecastTime.get(1);

    List<JsonNode> deduplicatedItems = apiFetcher.fetchAllPages(baseDate, baseTime, location);
    Map<String, WeatherBuilderHelper> builders = new LinkedHashMap<>();

    Map<String, List<JsonNode>> itemsByDate = deduplicatedItems.stream()
        .collect(Collectors.groupingBy(
            item -> item.path("fcstDate").asText(),
            TreeMap::new,      // 문자열 key 기준 오름차순 정렬
            Collectors.toList()
        ));

    for (Map.Entry<String, List<JsonNode>> entry : itemsByDate.entrySet()) {
      String fcstDate = entry.getKey();         // 날짜
      List<JsonNode> itemsForDate = entry.getValue(); // 해당 날짜의 JsonNode 리스트

      String fcstTime = itemsForDate.getFirst().path("fcstTime").asText();
      String key = fcstDate + ":" + fcstTime;

      WeatherBuilderHelper builder = builders.computeIfAbsent(key,
          k -> new WeatherBuilderHelper(baseDate, baseTime, fcstDate, fcstTime, location,
              processors));
      for (JsonNode item : itemsForDate) {
        if (!item.path("baseDate").asText().equals(baseDate)) {
          continue;
        }
        builder.setCategoryValue(item.path("category").asText(), item.path("fcstValue").asText());

      }

    }

    List<Weather> latestWeather = new ArrayList<>();
    for (WeatherBuilderHelper builder : builders.values()) {
      latestWeather.add(builder.build());
      log.info("Weather built: {}", latestWeather);
    }

    return latestWeather;

  }
}

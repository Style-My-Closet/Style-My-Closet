package com.stylemycloset.weather.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.stylemycloset.location.Location;
import com.stylemycloset.weather.entity.Weather;
import com.stylemycloset.weather.processor.WeatherCategoryProcessor;
import com.stylemycloset.weather.repository.WeatherRepository;
import com.stylemycloset.weather.util.DateTimeUtils;
import com.stylemycloset.weather.util.WeatherApiFetcher;
import com.stylemycloset.weather.util.WeatherBuilderHelper;
import com.stylemycloset.weather.util.WeatherBuilderHelperContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ForecastApiService {

  private final WeatherApiFetcher apiFetcher;
  private final List<WeatherCategoryProcessor> processors;
  private final WeatherRepository weatherRepository;

  public List<Weather> fetchData(Location location) {
    LocalDateTime now = LocalDateTime.now();
    List<String> forecastTime = DateTimeUtils.toBaseDateAndTime(now);
    String baseDate = forecastTime.get(0);
    String baseTime = forecastTime.get(1);

    List<JsonNode> deduplicatedItems = apiFetcher.fetchAllPages(baseDate, baseTime, location);
    Map<String, WeatherBuilderHelper> builders = new LinkedHashMap<>();
    List<WeatherBuilderHelperContext> bhcs = new ArrayList<>();

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
        builder.setCategoryValue(item.path("category").asText(),
            item.path("fcstValue").asText());

      }
      bhcs.add(builder.getContext());
    }

    List<Weather> latestWeather = new ArrayList<>();
    int i = 0;
    LocalDate today = LocalDate.now();
    LocalDateTime startOfYesterday = today.minusDays(1).atStartOfDay();
    LocalDateTime endOfYesterday = today.atStartOfDay().minusNanos(1);

    double TMPyesterday = 0.0;
    double HUMIDyesterday = 0.0;
    double WINDyesterday = 0.0;

    for (
        WeatherBuilderHelper builder : builders.values()) {
      if (i != 0) {
        TMPyesterday = bhcs.get(i - 1).temperature.getCurrent();
        HUMIDyesterday = bhcs.get(i - 1).humidity.getCurrent();
        WINDyesterday = bhcs.get(i - 1).windSpeed.getCurrent();
        bhcs.get(i).setCompareToDayBefore(TMPyesterday, HUMIDyesterday, WINDyesterday);
      } else {
        List<Weather> datas =
            weatherRepository.findWeathersByForecastedAtYesterday(location.getId(),
                startOfYesterday, endOfYesterday);

        if (!datas.isEmpty()) {
          TMPyesterday = datas.getLast().getTemperature().getCurrent();
          HUMIDyesterday = datas.getLast().getHumidity().getCurrent();
          WINDyesterday = datas.getLast().getWindSpeed().getCurrent();
          bhcs.getFirst().setCompareToDayBefore(TMPyesterday, HUMIDyesterday, WINDyesterday);
        }
      }
      latestWeather.add(builder.build());
      i++;
    }

    return latestWeather;

  }
}
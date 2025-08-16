package com.stylemycloset.weather.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.stylemycloset.location.Location;
import com.stylemycloset.location.LocationRepository;
import com.stylemycloset.weather.entity.Weather;
import com.stylemycloset.weather.processor.WeatherCategoryProcessor;
import com.stylemycloset.weather.util.DateTimeUtils;
import com.stylemycloset.weather.util.WeatherApiFetcher;
import com.stylemycloset.weather.util.WeatherBuilderHelper;
import com.stylemycloset.weather.util.WeatherItemsFilterer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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
        Map<String, WeatherBuilderHelper> builders = new HashMap<>();

        Map<String, List<JsonNode>> itemsByDate = deduplicatedItems.stream()
            .collect(Collectors.groupingBy(item -> item.path("fcstDate").asText()));

        for (JsonNode item : deduplicatedItems) {
            if (!item.path("baseDate").asText().equals(baseDate)) continue;

            String fcstDate = item.path("fcstDate").asText();
            String fcstTime = item.path("fcstTime").asText();
            String key = fcstDate + "|" + fcstTime + "|" + item.path("nx").asText() + "," + item.path("ny").asText();

            WeatherBuilderHelper builder = builders.computeIfAbsent(key,
                k -> new WeatherBuilderHelper(baseDate, baseTime, fcstDate, fcstTime, location,processors));
            builder.setCategoryValue(item.path("category").asText(), item.path("fcstValue").asText());


        }

        List<Weather> latestWeather = new ArrayList<>();
        for (WeatherBuilderHelper builder : builders.values()) {
            latestWeather.add(builder.build()) ;
            log.info("Weather built: {}", latestWeather);
        }


        return latestWeather;

    }
}

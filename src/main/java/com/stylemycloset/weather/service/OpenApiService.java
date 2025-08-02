package com.stylemycloset.weather.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import com.stylemycloset.location.Location;
import com.stylemycloset.location.LocationRepository;
import com.stylemycloset.weather.entity.Weather;
import com.stylemycloset.weather.processor.WeatherCategoryProcessor;
import com.stylemycloset.weather.repository.WeatherRepository;
import com.stylemycloset.weather.util.WeatherApiFetcher;
import com.stylemycloset.weather.util.WeatherBuilderHelper;
import com.stylemycloset.weather.util.WeatherItemDeduplicator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenApiService {

    private final WeatherApiFetcher apiFetcher;
    private final WeatherItemDeduplicator deduplicator;
    private final WeatherRepository weatherRepository;
    private final LocationRepository locationRepository;
    private final List<WeatherCategoryProcessor> processors;

    public void fetchData(String baseDate, String baseTime, Location location) {
        List<JsonNode> rawItems = apiFetcher.fetchAllPages(baseDate, baseTime, location);
        List<JsonNode> deduplicatedItems = deduplicator.deduplicate(rawItems);
        Map<String, WeatherBuilderHelper> builders = new HashMap<>();

        for (JsonNode item : deduplicatedItems) {
            if (!item.path("baseDate").asText().equals(baseDate)) continue;

            String fcstDate = item.path("fcstDate").asText();
            String fcstTime = item.path("fcstTime").asText();
            String key = fcstDate + "|" + fcstTime + "|" + item.path("nx").asText() + "," + item.path("ny").asText();

            WeatherBuilderHelper builder = builders.computeIfAbsent(key,
                k -> new WeatherBuilderHelper(baseDate, baseTime, fcstDate, fcstTime, location,processors));
            builder.setCategoryValue(item.path("category").asText(), item.path("fcstValue").asText());


        }

        Weather latestWeather = null;
        for (WeatherBuilderHelper builder : builders.values()) {
            latestWeather = builder.build();
            log.info("Weather built: {}", latestWeather);
        }

        weatherRepository.save(Optional.ofNullable(latestWeather).orElseThrow(() ->
            new StyleMyClosetException(ErrorCode.ERROR_CODE, Map.of("location", location))));
        locationRepository.save(latestWeather.getLocation());
    }
}

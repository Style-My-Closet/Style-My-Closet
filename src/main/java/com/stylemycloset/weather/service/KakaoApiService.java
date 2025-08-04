package com.stylemycloset.weather.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import com.stylemycloset.location.Location;
import com.stylemycloset.location.LocationRepository;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KakaoApiService {

    private final LocationRepository locationRepository;
    private final ObjectMapper objectMapper;

    @Value("${external.kakao.rest-api-key}")
    private String restApiKey;

    @Value("${external.kakao.base-url}")
    private String baseUrl;

    public Location createLocation(double latitude, double longitude) {
        JsonNode documents = fetchDocumentsFromKakao(latitude, longitude);
        Location location = buildLocationFromJson(documents);
        return locationRepository.save(location);
    }

    private JsonNode fetchDocumentsFromKakao(double latitude, double longitude) {
        try {
            String apiUrl = String.format("%s?x=%f&y=%f", baseUrl, latitude, longitude);
            URI uri = new URI(apiUrl);

            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", restApiKey);

            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                log.error("Kakao API 호출 실패: status = {}, url = {}", status, apiUrl);
                throw new RuntimeException("Kakao API 호출 실패");
            }

            try (InputStream inputStream = connection.getInputStream()) {
                return objectMapper.readTree(inputStream).path("documents");
            }

        } catch (Exception e) {
            log.error("Kakao API 호출 중 오류 발생", e);
            throw new StyleMyClosetException(ErrorCode.ERROR_CODE, Map.of("apiError", "Kakao API 호출 오류"));
        }
    }

    private Location buildLocationFromJson(JsonNode documentsNode) {
        if (documentsNode == null || !documentsNode.isArray() || documentsNode.isEmpty()) {
            throw new StyleMyClosetException(ErrorCode.ERROR_CODE, Map.of("document", "응답 데이터 없음"));
        }

        JsonNode doc = documentsNode.get(0);

        Integer x = doc.path("x").asInt(); // 경도
        Integer y = doc.path("y").asInt(); // 위도

        JsonNode address = doc.path("address");

        List<String> locationNames = List.of(
            address.path("region_1depth_name").asText(),
            address.path("region_2depth_name").asText(),
            address.path("region_3depth_name").asText()
        );

        return Location.builder()
            .x(x)
            .y(y)
            .latitude(y.doubleValue())
            .longitude(x.doubleValue())
            .locationNames(locationNames)
            .build();
    }
}

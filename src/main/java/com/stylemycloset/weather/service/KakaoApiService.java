package com.stylemycloset.weather.service;



import static com.stylemycloset.location.util.LamcConverter.lamcProj;
import static com.stylemycloset.location.util.LamcConverter.mapConv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import com.stylemycloset.location.Location;
import com.stylemycloset.location.LocationRepository;
import com.stylemycloset.location.util.LamcConverter;
import com.stylemycloset.weather.dto.LocationInfo;
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

    public Location createLocation(double longitude, double latitude) {
        JsonNode documents = fetchDocumentsFromKakao(longitude, latitude);
        Location location = buildLocationFromJson(documents,longitude,latitude);
        return locationRepository.save(location);
    }

    public JsonNode fetchDocumentsFromKakao(double longitude, double latitude) {
        try {
            String apiUrl = String.format("%s?x=%f&y=%f", baseUrl, longitude, latitude);
            URI uri = new URI(apiUrl);

            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "KakaoAK " + restApiKey);

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

    private Location buildLocationFromJson(JsonNode documentsNode, double longitude, double latitude) {
        JsonNode document = extractValidDocument(documentsNode);
        LocationInfo locationInfo = parseLocationInfo(document, longitude, latitude);
        return buildLocationFromInfo(locationInfo);
    }

    private JsonNode extractValidDocument(JsonNode documentsNode) {
        if (documentsNode == null || !documentsNode.isArray() || documentsNode.isEmpty()) {
            throw new StyleMyClosetException(ErrorCode.ERROR_CODE, Map.of("document", "응답 데이터 없음"));
        }
        return documentsNode.get(0);
    }

    private LocationInfo parseLocationInfo(JsonNode doc, double x, double y) {
        List<String> locationNames = List.of(
            doc.path("region_1depth_name").asText(),
            doc.path("region_2depth_name").asText(),
            doc.path("region_3depth_name").asText()
        );

        return new LocationInfo(x, y, locationNames);
    }

    private Location buildLocationFromInfo(LocationInfo info) {
        double[] xy = mapConv(info.x(), info.y(), 0);
        return Location.builder()
            .x((int)xy[0])
            .y((int)xy[1])
            .latitude(info.y())
            .longitude(info.x())
            .locationNames(info.locationNames())
            .build();
    }

}

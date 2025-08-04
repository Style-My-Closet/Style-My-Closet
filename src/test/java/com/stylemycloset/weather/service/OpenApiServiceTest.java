package com.stylemycloset.weather.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stylemycloset.location.Location;
import com.stylemycloset.weather.repository.WeatherRepository;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class OpenApiServiceTest {

    @Mock
    private WeatherRepository weatherRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private openApiService openApiService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(openApiService, "serviceKey", System.getenv("SERVICE_KEY"));
        ReflectionTestUtils.setField(openApiService, "baseUrl", "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0");
        ReflectionTestUtils.setField(openApiService, "numOfRows", 100);
    }

    private final String mockResponseJson = """
    {  
        "response": {
        "header": {
          "resultCode": "00",
          "resultMsg": "NORMAL_SERVICE"
        },
        "body": {
          "dataType": "JSON",
          "items": {
            "item": [
              {
                "baseDate": "20250801",
                "baseTime": "0230",
                "category": "LGT",
                "fcstDate": "20250801",
                "fcstTime": "0300",
                "fcstValue": "0",
                "nx": 55,
                "ny": 127
              },
              {
                "baseDate": "20250801",
                "baseTime": "0230",
                "category": "LGT",
                "fcstDate": "20250801",
                "fcstTime": "0400",
                "fcstValue": "0",
                "nx": 55,
                "ny": 127
              },
              {
                "baseDate": "20250801",
                "baseTime": "0230",
                "category": "LGT",
                "fcstDate": "20250801",
                "fcstTime": "0500",
                "fcstValue": "0",
                "nx": 55,
                "ny": 127
              },
              {
                "baseDate": "20250801",
                "baseTime": "0230",
                "category": "LGT",
                "fcstDate": "20250801",
                "fcstTime": "0600",
                "fcstValue": "0",
                "nx": 55,
                "ny": 127
              },
              {
                "baseDate": "20250801",
                "baseTime": "0230",
                "category": "LGT",
                "fcstDate": "20250801",
                "fcstTime": "0700",
                "fcstValue": "0",
                "nx": 55,
                "ny": 127
              },
              {
                "baseDate": "20250801",
                "baseTime": "0230",
                "category": "LGT",
                "fcstDate": "20250801",
                "fcstTime": "0800",
                "fcstValue": "0",
                "nx": 55,
                "ny": 127
              },
              {
                "baseDate": "20250801",
                "baseTime": "0230",
                "category": "PTY",
                "fcstDate": "20250801",
                "fcstTime": "0300",
                "fcstValue": "0",
                "nx": 55,
                "ny": 127
              },
              {
                "baseDate": "20250801",
                "baseTime": "0230",
                "category": "PTY",
                "fcstDate": "20250801",
                "fcstTime": "0400",
                "fcstValue": "0",
                "nx": 55,
                "ny": 127
              },
              {
                "baseDate": "20250801",
                "baseTime": "0230",
                "category": "PTY",
                "fcstDate": "20250801",
                "fcstTime": "0500",
                "fcstValue": "0",
                "nx": 55,
                "ny": 127
              },
              {
                "baseDate": "20250801",
                "baseTime": "0230",
                "category": "PTY",
                "fcstDate": "20250801",
                "fcstTime": "0600",
                "fcstValue": "0",
                "nx": 55,
                "ny": 127
              }
            ]
          },
          "pageNo": 1,
          "numOfRows": 10,
          "totalCount": 60
        }
      }
    }

        """;

    @Test
    void fetchData_shouldProcessWeatherItemsSuccessfully() throws Exception {
        // given
        Location location = Location.builder()
            .x(55).y(127)
            .latitude(37.5665)
            .longitude(126.9780)
            .locationNames(List.of("서울", "중구"))
            .build();

        JsonNode jsonNode = new ObjectMapper().readTree(mockResponseJson)
        .path("response").path("body").path("items").path("item");
        when(restTemplate.getForObject(any(URI.class), eq(String.class)))
            .thenReturn(mockResponseJson);

        when(objectMapper.readTree(mockResponseJson)).thenReturn(jsonNode);

        // when
        openApiService.fetchData("20250801", "0200", location);

        // then
        // 여기에 저장 로직까지 넣는다면 save 호출 여부 확인 가능
        // verify(weatherRepository).save(any(Weather.class));
    }
}


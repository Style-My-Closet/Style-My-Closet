package com.stylemycloset.weather.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stylemycloset.location.Location;
import com.stylemycloset.location.LocationRepository;
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
class KakaoApiServiceTest {

    @InjectMocks
    private KakaoApiService kakaoApiService;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(kakaoApiService, "restApiKey", "374eaf9560d79672620c21e26416635a");
        ReflectionTestUtils.setField(kakaoApiService, "baseUrl", "https://dapi.kakao.com/v2/local/geo/coord2regioncode.json");
    }

    @Test
    void createLocation_shouldReturnLocation() throws Exception {
        // given
        double latitude = 37.5665;
        double longitude = 126.9780;

        String jsonResponse = """
            {
                    "meta": {
                        "total_count": 2
                    },
                    "documents": [
                        {
                            "region_type": "B",
                            "code": "1114010300",
                            "address_name": "서울특별시 중구 태평로1가",
                            "region_1depth_name": "서울특별시",
                            "region_2depth_name": "중구",
                            "region_3depth_name": "태평로1가",
                            "region_4depth_name": "",
                            "x": 126.97723484374212,
                            "y": 37.56770871576262
                        },
                        {
                            "region_type": "H",
                            "code": "1114055000",
                            "address_name": "서울특별시 중구 명동",
                            "region_1depth_name": "서울특별시",
                            "region_2depth_name": "중구",
                            "region_3depth_name": "명동",
                            "region_4depth_name": "",
                            "x": 126.98581171546633,
                            "y": 37.56004798513031
                        }
                    ]
                }
        """;

        JsonNode jsonNode = new ObjectMapper().readTree(jsonResponse);
        JsonNode documentsNode = jsonNode.path("documents");

        // ObjectMapper는 직접 new 해서 사용했기 때문에 mocking이 아닌 real 동작으로 처리
        ObjectMapper realObjectMapper = new ObjectMapper();
        ReflectionTestUtils.setField(kakaoApiService, "objectMapper", realObjectMapper);


        Location dummyLocation = Location.builder()
            .latitude(37.56770871576262)
            .longitude(126.97723484374212)
            .x(126)
            .y(37)
            .locationNames(List.of("서울특별시", "중구", "태평로1가"))
            .build();
        when(locationRepository.save(any(Location.class))).thenReturn(dummyLocation);

        // when
        Location result = kakaoApiService.createLocation(longitude, latitude);


        // then
        assertNotNull(result);
        assertEquals("서울특별시", result.getLocationNames().get(0));
        assertEquals("중구", result.getLocationNames().get(1));
        assertEquals("태평로1가", result.getLocationNames().get(2));
    }
}


package com.stylemycloset.location;

import static org.hamcrest.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stylemycloset.weather.service.KakaoApiService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KakaoApiServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private KakaoApiService kakaoApiService;

    @Test
    @DisplayName("createLocation에서 받은 위도 경도 buildLocation에 잘 전달되나 ")
    void createLocation_shouldPassLatitudeLongitudeCorrectly() throws Exception {
        // given
        double longitude = 127.1234;
        double latitude = 37.5678;

        // Kakao API 가 반환할 가짜 documents JSON
        ObjectMapper realMapper = new ObjectMapper();
        String fakeResponse = """
            {
              "documents": [
                {
                  "region_1depth_name": "서울특별시",
                  "region_2depth_name": "강남구",
                  "region_3depth_name": "역삼동"
                }
              ]
            }
            """;
        JsonNode fakeDocuments = realMapper.readTree(fakeResponse).path("documents");

        // fetchDocumentsFromKakao를 직접 호출하면 HttpURLConnection으로 실제 요청을 보내므로,
        // Reflection으로 private 메서드를 대체하거나 Spy로 가로채야 함
        KakaoApiService spyService = Mockito.spy(kakaoApiService);
        doReturn(fakeDocuments).when(spyService).fetchDocumentsFromKakao(longitude, latitude);

        // LocationRepository save Mock
        ArgumentCaptor<Location> captor = ArgumentCaptor.forClass(Location.class);
        when(locationRepository.save(ArgumentMatchers.<Location>any())).thenAnswer(invocation -> (Location) invocation.getArgument(0));

        // when
        Location location = spyService.createLocation(longitude, latitude);

        // then
        verify(locationRepository).save(captor.capture());
        Location saved = captor.getValue();

        assertThat(saved.getLongitude()).isEqualTo(longitude);
        assertThat(saved.getLatitude()).isEqualTo(latitude);
        assertThat(saved.getLocationNames()).contains("서울특별시", "강남구", "역삼동");
    }
}

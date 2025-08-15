package com.stylemycloset.cloth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
// @WebMvcTest에서는 이러한 import들이 불필요
import org.springframework.boot.test.mock.mockito.MockBean;
import com.stylemycloset.cloth.service.ClothListCacheService;
import com.stylemycloset.cloth.service.ClothService;
import com.stylemycloset.cloth.service.ClothProductExtractionService;
import org.mockito.Mockito;
import org.junit.jupiter.api.BeforeEach;
import com.stylemycloset.cloth.dto.CursorDto;
import jakarta.persistence.EntityManager;
import com.stylemycloset.cloth.dto.ClothCreateRequestDto;
import org.springframework.mock.web.MockPart;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import com.stylemycloset.cloth.entity.ClothingCategoryType;
import com.stylemycloset.cloth.dto.ClothResponseDto;
import com.stylemycloset.cloth.dto.response.ClothListResponseDto;
import com.stylemycloset.cloth.dto.response.ClothItemDto;
import com.stylemycloset.cloth.dto.AttributeDto;
import com.stylemycloset.cloth.dto.SortDirection;
import org.junit.jupiter.api.Assumptions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.util.List;
import java.util.function.Supplier;


@WebMvcTest(ClothingController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class ClothingControllerIntegrationTest {


    @Autowired
    private ObjectMapper objectMapper;
    


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClothListCacheService clothListCacheService;

    @MockBean
    private EntityManager entityManager;
    
    @MockBean
    private ClothService clothService;
    
    @MockBean
    private ClothProductExtractionService clothProductExtractionService;
    @BeforeEach
    void setup() {
        Mockito.lenient().when(clothListCacheService.isFirstPage(Mockito.any(CursorDto.class))).thenReturn(true);
        Mockito.lenient().when(clothListCacheService.isDefaultSort(Mockito.any(CursorDto.class))).thenReturn(true);
        Mockito.lenient().when(clothListCacheService.isNoKeywordSearch(Mockito.any(CursorDto.class))).thenReturn(true);
        Mockito.lenient().when(clothListCacheService.getAttributeListFirstPage(Mockito.any()))
                .thenAnswer(invocation -> invocation.getArgument(0, Supplier.class).get());
        Mockito.doNothing().when(clothListCacheService).evictAttributeListFirstPage();
        Mockito.lenient().when(clothListCacheService.getClothListFirstPage(Mockito.anyLong(), Mockito.any()))
                .thenAnswer(invocation -> invocation.getArgument(1, Supplier.class).get());
        Mockito.doNothing().when(clothListCacheService).evictClothListFirstPage(Mockito.anyLong());

        // ClothService mocks
        ClothResponseDto mockResponse = new ClothResponseDto();
        mockResponse.setId(1L);
        mockResponse.setOwnerId(1L);
        mockResponse.setName("컨트롤러생성");
        mockResponse.setType(ClothingCategoryType.TOP);
        mockResponse.setImageUrl(null);
        mockResponse.setAttributes(List.of(
            new AttributeDto(1L, "색상", List.of("빨강", "파랑"), "빨강"),
            new AttributeDto(2L, "사이즈", List.of("S", "M", "L"), "M")
        ));
        
        Mockito.when(clothService.createClothWithImage(Mockito.any(), Mockito.any(), Mockito.anyLong()))
                .thenReturn(mockResponse);
        
        Mockito.doNothing().when(clothService).deleteCloth(Mockito.anyLong());
        
        // ClothService GET API mock  
        ClothItemDto mockItemDto = new ClothItemDto(1L, 1L, "컨트롤러생성", null, ClothingCategoryType.TOP);
        mockItemDto.setAttributes(List.of(
            new AttributeDto(1L, "색상", List.of("빨강", "파랑"), "빨강"),
            new AttributeDto(2L, "사이즈", List.of("S", "M", "L"), "M")
        ));
        
        ClothListResponseDto mockListResponse = new ClothListResponseDto(
                List.of(mockItemDto),
                null,
                null,
                false,
                1L,
                "createdAt",
                SortDirection.ASCENDING
        );
        
        Mockito.when(clothService.getClothesWithCursor(Mockito.any(), Mockito.any()))
                .thenReturn(mockListResponse);
                
        Mockito.when(clothService.createClothWithImage(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(mockResponse);
                
        Mockito.doNothing().when(clothService).deleteCloth(Mockito.anyLong());

    }

    @Test
    void 실제_URL_API_테스트_mocking_또는_스킵() throws Exception {
        Assumptions.assumeTrue(false, "외부 호출 테스트는 스킵");
    }

    @Test
    void 의류_목록_페이징_API_테스트() throws Exception {
        // 1페이지 호출 (limit=3)
        mockMvc.perform(MockMvcRequestBuilders.get("/api/clothes")
                        .param("limit", "3")
                        .param("sortBy", "createdAt")
                        .param("sortDirection", "asc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(org.hamcrest.Matchers.lessThanOrEqualTo(3)))
                .andExpect(jsonPath("$.hasNext").exists())
                .andExpect(jsonPath("$.nextCursor").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.totalCount").exists())
                .andExpect(jsonPath("$.sortBy").value("createdAt"))
                .andExpect(jsonPath("$.sortDirection").value("ASCENDING"))
                .andDo(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    System.out.println("=== 1페이지 응답 ===");
                    System.out.println(responseBody);
                });


    }

    @Test
    void 의류_생성_삭제_API_JSON_검증() throws Exception {


        // 생성 요청 바디
        ClothCreateRequestDto req = new ClothCreateRequestDto();
        req.setName("컨트롤러생성");
        req.setType("TOP");

        String body = objectMapper.writeValueAsString(req);

        // 생성 (멀티파트 요청으로 변경)
        MockPart requestPart = new MockPart("request", body.getBytes());
        requestPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        String createdId = mockMvc.perform(MockMvcRequestBuilders.multipart("/api/clothes")
                        .part(requestPart))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("컨트롤러생성"))
                .andExpect(jsonPath("$.type").value("TOP"))
                .andExpect(jsonPath("$.imageUrl").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.attributes").isArray())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 응답에서 id 추출
        String id = objectMapper.readTree(createdId).get("id").asText();

        // 목록 확인 (생성 반영)
        mockMvc.perform(MockMvcRequestBuilders.get("/api/clothes")
                        .param("limit", "10")
                        .param("sortBy", "createdAt")
                        .param("sortDirection", "asc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));

        // 삭제
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/clothes/" + id))
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/clothes")
                        .param("limit", "10")
                        .param("sortBy", "createdAt")
                        .param("sortDirection", "asc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(1))  // Mock이므로 여전히 1
                .andExpect(jsonPath("$.data.length()").value(1));
    }
} 
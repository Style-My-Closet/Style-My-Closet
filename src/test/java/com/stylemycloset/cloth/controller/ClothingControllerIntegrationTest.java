package com.stylemycloset.cloth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stylemycloset.cloth.dto.ClothExtractionRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.stylemycloset.testutil.IntegrationTestSupport;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.stylemycloset.cloth.service.ClothListCacheService;
import org.mockito.Mockito;
import org.junit.jupiter.api.BeforeEach;
import com.stylemycloset.cloth.dto.CursorDto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class ClothingControllerIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @MockBean
    private ClothListCacheService clothListCacheService;

    @BeforeEach
    void setup() {
        Mockito.lenient().when(clothListCacheService.isFirstPage(Mockito.any(CursorDto.class))).thenReturn(true);
        Mockito.lenient().when(clothListCacheService.isDefaultSort(Mockito.any(CursorDto.class))).thenReturn(true);
        Mockito.lenient().when(clothListCacheService.isNoKeywordSearch(Mockito.any(CursorDto.class))).thenReturn(true);
        Mockito.lenient().when(clothListCacheService.getAttributeListFirstPage(Mockito.any()))
                .thenAnswer(invocation -> invocation.getArgument(0, java.util.function.Supplier.class).get());
        Mockito.doNothing().when(clothListCacheService).evictAttributeListFirstPage();
        Mockito.lenient().when(clothListCacheService.getClothListFirstPage(Mockito.anyLong(), Mockito.any()))
                .thenAnswer(invocation -> invocation.getArgument(1, java.util.function.Supplier.class).get());
        Mockito.doNothing().when(clothListCacheService).evictClothListFirstPage(Mockito.anyLong());
    }

    @Test
    void 실제_무신사_URL_API_테스트() throws Exception {
        // given
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        String musinsaUrl = "https://www.musinsa.com/products/4055686";

        // when & then
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/clothes/extractions")
                        .param("productUrl", musinsaUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").isNotEmpty())
                .andExpect(jsonPath("$.imageUrl").value(musinsaUrl))
                .andDo(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    System.out.println("=== API 응답 ===");
                    System.out.println(responseBody);
                });
    }

    @Test
    void 의류_목록_페이징_API_테스트() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // 1페이지 호출 (limit=3)
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/clothes")
                        .param("limit", "3")
                        .param("sortBy", "createdAt")
                        .param("sortDirection", "asc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(org.hamcrest.Matchers.lessThanOrEqualTo(3)))
                .andExpect(jsonPath("$.hasNext").exists())
                .andExpect(jsonPath("$.nextCursor").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.totalCount").exists())
                .andDo(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    System.out.println("=== 1페이지 응답 ===");
                    System.out.println(responseBody);
                });


    }
} 
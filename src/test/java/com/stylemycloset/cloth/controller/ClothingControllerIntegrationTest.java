package com.stylemycloset.cloth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import jakarta.persistence.EntityManager;
import com.stylemycloset.cloth.dto.ClothCreateRequestDto;

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

    @Autowired
    private EntityManager entityManager; // kept for future DB assertions if needed

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

        // 별도 SQL 스크립트로 초기화되므로 이곳에서는 캐시 모킹만 유지
    }

    @Test
    void 실제_URL_API_테스트_mocking_또는_스킵() throws Exception {
        // 외부 의존(E2E)은 통합 테스트에서 불안정하므로 스킵 처리
        org.junit.jupiter.api.Assumptions.assumeTrue(false, "외부 호출 테스트는 스킵");
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
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // 생성 요청 바디
        ClothCreateRequestDto req = new ClothCreateRequestDto();
        req.setName("컨트롤러생성");
        req.setType("TOP");

        String body = objectMapper.writeValueAsString(req);

        // 생성
        String createdId = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/clothes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
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
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/clothes")
                        .param("limit", "10")
                        .param("sortBy", "createdAt")
                        .param("sortDirection", "asc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));

        // 삭제
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/clothes/" + id))
                .andDo(print())
                .andExpect(status().isOk());

        // 목록 재확인 (삭제 반영)
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/clothes")
                        .param("limit", "10")
                        .param("sortBy", "createdAt")
                        .param("sortDirection", "asc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(0))
                .andExpect(jsonPath("$.data.length()").value(0));
    }
} 
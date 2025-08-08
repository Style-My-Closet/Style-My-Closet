package com.stylemycloset.ootd.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stylemycloset.cloth.entity.Closet;
import com.stylemycloset.cloth.entity.Cloth;
import com.stylemycloset.cloth.entity.ClothingCategory;
import com.stylemycloset.cloth.entity.ClothingCategoryType;
import com.stylemycloset.cloth.repository.ClosetRepository;
import com.stylemycloset.cloth.repository.ClothRepository;
import com.stylemycloset.cloth.repository.ClothingCategoryRepository;
import com.stylemycloset.ootd.dto.FeedCreateRequest;
import com.stylemycloset.IntegrationTestSupport;
import com.stylemycloset.user.entity.Role;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
@AutoConfigureMockMvc
@Transactional
public class FeedControllerTest extends IntegrationTestSupport {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private ClothRepository clothRepository;
  @Autowired
  private ClothingCategoryRepository categoryRepository;
  @Autowired
  private ClosetRepository closetRepository;

  private User testUser;
  private Cloth testCloth1;
  private Cloth testCloth2;

  @BeforeEach
  void setUp() {

    testUser = new User();
    ReflectionTestUtils.setField(testUser, "name", "test");
    ReflectionTestUtils.setField(testUser, "email", "test@test.com");
    ReflectionTestUtils.setField(testUser, "password", "password");
    ReflectionTestUtils.setField(testUser, "role", Role.USER);
    ReflectionTestUtils.setField(testUser, "locked", false);
    userRepository.save(testUser);

    Closet closet = new Closet(testUser);
    ReflectionTestUtils.setField(closet, "user", testUser);
    closetRepository.save(closet);

    ClothingCategory category = new ClothingCategory(ClothingCategoryType.TOP);
    categoryRepository.save(category);

    testCloth1 = clothRepository.save(Cloth.createCloth("옷1", closet, category, null));

    testCloth2 = clothRepository.save(Cloth.createCloth("옷2", closet, category, null));
  }

  @Test
  @DisplayName("새로운 OOTD 피드를 등록하면 201 Created 상태와 함께 피드 정보를 반환한다")
  @WithMockUser
  void createFeed_Returns201AndFeedDto() throws Exception {
    // given (준비)
    List<Long> clothesIds = List.of(testCloth1.getId(), testCloth2.getId());
    FeedCreateRequest request = new FeedCreateRequest(testUser.getId(), null, clothesIds, "통합 테스트 피드 내용");
    String requestJson = objectMapper.writeValueAsString(request);

    // when & then (실행 및 검증)
    mockMvc.perform(post("/api/feeds")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson)
            .with(csrf()))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.content").value("통합 테스트 피드 내용"))
        .andExpect(jsonPath("$.author.userId").value(testUser.getId()))
        .andExpect(jsonPath("$.ootds.length()").value(2));
  }
}
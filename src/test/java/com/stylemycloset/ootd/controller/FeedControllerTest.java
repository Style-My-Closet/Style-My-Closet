package com.stylemycloset.ootd.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import com.stylemycloset.ootd.entity.Feed;
import com.stylemycloset.ootd.entity.FeedLike;
import com.stylemycloset.ootd.repo.FeedLikeRepository;
import com.stylemycloset.ootd.repo.FeedRepository;
import com.stylemycloset.testutil.IntegrationTestSupport;
import com.stylemycloset.user.entity.Role;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
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
  @Autowired
  private FeedRepository feedRepository;
  @Autowired
  private FeedLikeRepository feedLikeRepository;

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

  @Nested
  @DisplayName("피드 생성 및 조회 API")
  class FeedCreateAndGet {

    @Test
    @DisplayName("새로운 OOTD 피드를 등록하면 201 Created 상태와 함께 피드 정보를 반환한다")
    @WithMockUser
    void createFeed_Returns201AndFeedDto() throws Exception {
      // given (준비)
      List<Long> clothesIds = List.of(testCloth1.getId(), testCloth2.getId());
      FeedCreateRequest request = new FeedCreateRequest(testUser.getId(), null, clothesIds,
          "통합 테스트 피드 내용");
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

    @Test
    @DisplayName("피드 목록을 조회하면 200OK 상태와 함께 페이지된 피드 목록을 반환한다")
    @WithMockUser
    void getFeeds_Returns200AndPagedFeeds() throws Exception {
      for (int i = 0; i < 11; i++) {
        feedRepository.save(Feed.createFeed(testUser, null, "테스트 피드 " + i));
      }

      mockMvc.perform(get("/api/feeds")
              .param("size", "10"))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.length()").value(10))
          .andExpect(jsonPath("$.hasNext").value(true));
    }
  }

  @Nested
  @DisplayName("피드 좋아요 API")
  class FeedLikeApi {

    @Test
    @DisplayName("피드에 좋아요를 누르면 200 OK 상태와 함께 'likedByMe'가 true로 반환된다")
    @WithMockUser
      // 현재 Controller에서 사용자 ID를 1L로 하드코딩했으므로, 이 테스트는 ID 1번 유저가 수행하는 셈
    void likeFeed_Returns200AndUpdatedFeed() throws Exception {
      // given (준비)
      Feed feed = feedRepository.save(Feed.createFeed(testUser, null, "좋아요 테스트용 피드"));

      // when & then (실행 및 검증)
      mockMvc.perform(post("/api/feeds/{feedId}/like", feed.getId())
              .with(csrf()))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.likedByMe").value(true))
          .andExpect(jsonPath("$.likeCount").value(1));
    }

    @Test
    @DisplayName("이미 좋아요를 누른 피드에 다시 요청하면 404 Not Found를 반환한다")
    @WithMockUser
    void likeFeed_WhenAlreadyLiked_Returns404() throws Exception {
      // given (준비)
      Feed feed = feedRepository.save(Feed.createFeed(testUser, null, "좋아요 테스트용 피드"));
      // 미리 '좋아요' 상태를 만들어둠
      feedLikeRepository.save(FeedLike.createFeedLike(testUser, feed));

      // when & then (실행 및 검증)
      mockMvc.perform(post("/api/feeds/{feedId}/like", feed.getId())
              .with(csrf()))
          .andDo(print())
          .andExpect(status().isNotFound()) // ErrorCode에 정의된 HttpStatus
          .andExpect(jsonPath("$.errorCodeName").value("ALREADY_LIKED_FEED"));
    }

    @Test
    @DisplayName("좋아요를 누른 피드를 취소하면 204 No Content를 반환한다")
    @WithMockUser
    void unlikeFeed_Returns204() throws Exception {
      // given (준비)
      Feed feed = feedRepository.save(Feed.createFeed(testUser, null, "좋아요 테스트용 피드"));
      feedLikeRepository.save(FeedLike.createFeedLike(testUser, feed));

      // when & then (실행 및 검증)
      mockMvc.perform(delete("/api/feeds/{feedId}/like", feed.getId())
              .with(csrf()))
          .andDo(print())
          .andExpect(status().isNoContent());
    }
  }
}
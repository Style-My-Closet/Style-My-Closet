package com.stylemycloset.ootd.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import com.stylemycloset.ootd.dto.CommentCreateRequest;
import com.stylemycloset.ootd.dto.FeedCreateRequest;
import com.stylemycloset.ootd.dto.FeedUpdateRequest;
import com.stylemycloset.ootd.entity.Feed;
import com.stylemycloset.ootd.entity.FeedComment;
import com.stylemycloset.ootd.entity.FeedLike;
import com.stylemycloset.ootd.repo.FeedCommentRepository;
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
  @Autowired
  private FeedCommentRepository feedCommentRepository;

  private User testUser;
  private Cloth testCloth1;
  private Cloth testCloth2;


  @BeforeEach
  void setUp() {

    testUser = new User();
    ReflectionTestUtils.setField(testUser, "name", "test");
    ReflectionTestUtils.setField(testUser, "email", "user");
    ReflectionTestUtils.setField(testUser, "password", "password");
    ReflectionTestUtils.setField(testUser, "role", Role.USER);
    ReflectionTestUtils.setField(testUser, "locked", false);
    userRepository.save(testUser);

    Closet closet = new Closet(testUser);
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
              .param("limit", "10")
              .param("sortBy", "createdAt")
              .param("sortDirection", "DESCENDING")
              .with(csrf())) // GET 요청이지만 CSRF 설정에 따라 필요할 수 있음
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.length()").value(10))
          .andExpect(jsonPath("$.hasNext").value(true));
    }
  }

  @Test
  @DisplayName("좋아요 토글 - 성공 시 (좋아요 추가) 200 OK와 업데이트된 피드 정보를 반환한다")
  @WithMockUser
  void toggleLike_whenNotLiked_returnsOkWithFeedDto() throws Exception {
    Feed feed = feedRepository.save(Feed.createFeed(testUser, null, "좋아요 테스트용 피드"));

    mockMvc.perform(post("/api/feeds/{feedId}/like", feed.getId())
            .with(csrf()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.likedByMe").value(true))
        .andExpect(jsonPath("$.likeCount").value(1));
  }

  @Test
  @DisplayName("좋아요 토글 - 성공 시 (좋아요 취소) 200 OK와 업데이트된 피드 정보를 반환한다")
  @WithMockUser
  void toggleLike_whenAlreadyLiked_returnsOkWithFeedDto() throws Exception {
    Feed feed = feedRepository.save(Feed.createFeed(testUser, null, "좋아요 테스트용 피드"));
    feedLikeRepository.save(FeedLike.createFeedLike(testUser, feed));

    mockMvc.perform(post("/api/feeds/{feedId}/like", feed.getId())
            .with(csrf()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.likedByMe").value(false))
        .andExpect(jsonPath("$.likeCount").value(0));
  }

  @Nested
  @DisplayName("피드 수정 API")
  class FeedUpdateApi {

    @Test
    @DisplayName("자신이 작성한 피드의 내용을 수정하면 200 OK와 함께 피드 정보를 반환한다.")
    @WithMockUser
    void updateFeed_Success() throws Exception {
      Feed myFeed = feedRepository.save(Feed.createFeed(testUser, null, "수정 전 피드 내용"));
      FeedUpdateRequest request = new FeedUpdateRequest("수정 후 새로운 피드 내용");
      String requestJson = objectMapper.writeValueAsString(request);

      mockMvc.perform(patch("/api/feeds/{feedId}", myFeed.getId())
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestJson)
              .with(csrf()))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(myFeed.getId()))
          .andExpect(jsonPath("$.content").value("수정 후 새로운 피드 내용"));
    }

    @Test
    @DisplayName("피드 내용을 비워서 수정 요청하면 400 Bad Request 상태를 반환한다")
    @WithMockUser
    void updateFeed_Fail_InvalidRequest() throws Exception {
      // given
      Feed myFeed = feedRepository.save(Feed.createFeed(testUser, null, "원래 내용"));
      FeedUpdateRequest request = new FeedUpdateRequest(" "); // 공백 문자열
      String requestJson = objectMapper.writeValueAsString(request);

      // when & then
      mockMvc.perform(patch("/api/feeds/{feedId}", myFeed.getId())
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestJson)
              .with(csrf()))
          .andDo(print())
          .andExpect(status().isBadRequest());
    }
  }


  @Test
  @DisplayName("피드 삭제 API - 자신이 작성한 피드를 삭제하면 204 No Content 상태를 반환")
  @WithMockUser
  void deleteFeed_Success() throws Exception {
    Feed myFeed = feedRepository.save(Feed.createFeed(testUser, null, "삭제될 피드"));

    mockMvc.perform(delete("/api/feeds/{feedId}", myFeed.getId())
            .with(csrf()))
        .andDo(print())
        .andExpect(status().isNoContent());

    assertThat(feedRepository.findById(myFeed.getId())).isEmpty();
  }

  @Nested
  @DisplayName("피드 댓글 조회 API")
  class FeedCommentApi {

    @Test
    @DisplayName("피드에 대한 댓글 목록을 조회하면 200 OK와 함께 페이지된 댓글 목록을 반환한다")
    @WithMockUser
    void getComments_Success() throws Exception {
      // given (준비)
      // 1. 테스트용 피드 생성
      Feed feed = feedRepository.save(Feed.createFeed(testUser, null, "댓글 테스트용 피드"));

      // 2. 해당 피드에 댓글 11개 저장 (다음 페이지가 있도록)
      for (int i = 0; i < 11; i++) {
        FeedComment comment = new FeedComment(feed, testUser, "테스트 댓글 " + i);
        feedCommentRepository.save(comment);
      }

      // when & then (실행 및 검증)
      mockMvc.perform(get("/api/feeds/{feedId}/comments", feed.getId())
              .param("limit", "10")
              .with(csrf()))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.length()").value(10))
          .andExpect(jsonPath("$.hasNext").value(true))
          .andExpect(jsonPath("$.data[0].content").value("테스트 댓글 10")); // 최신순 정렬 확인
    }

    @Test
    @DisplayName("존재하지 않는 피드의 댓글을 조회하면 404 Not Found를 반환한다")
    @WithMockUser
    void getComments_Fail_FeedNotFound() throws Exception {
      // given (준비)
      long nonExistentFeedId = 9999L;

      // when & then (실행 및 검증)
      mockMvc.perform(get("/api/feeds/{feedId}/comments", nonExistentFeedId)
              .param("limit", "10")
              .with(csrf()))
          .andDo(print())
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("피드 댓글 등록 API")
  class FeedCommentCreateApi {

    @Test
    @DisplayName("새로운 댓글을 등록하면 201 Created 상태와 함께 생성된 댓글 정보를 반환한다")
    @WithMockUser
    void createComment_Success() throws Exception {
      // given (준비)
      // 1. 댓글을 달 피드 생성
      Feed feed = feedRepository.save(Feed.createFeed(testUser, null, "댓글 등록용 피드"));

      // 2. 등록할 댓글 정보 DTO 생성 (Swagger 명세 기준)
      CommentCreateRequest request = new CommentCreateRequest(
          feed.getId(),
          testUser.getId(),
          "새로운 댓글입니다!"
      );
      String requestJson = objectMapper.writeValueAsString(request);

      // when & then (실행 및 검증)
      mockMvc.perform(post("/api/feeds/{feedId}/comments", feed.getId())
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestJson)
              .with(csrf()))
          .andDo(print())
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.content").value("새로운 댓글입니다!"))
          .andExpect(jsonPath("$.author.userId").value(testUser.getId()))
          .andExpect(jsonPath("$.feedId").value(feed.getId()));
    }

    @Test
    @DisplayName("댓글 내용이 비어있는 요청을 보내면 400 Bad Request를 반환한다")
    @WithMockUser
    void createComment_Fail_BlankContent() throws Exception {
      // given
      Feed feed = feedRepository.save(Feed.createFeed(testUser, null, "댓글 등록용 피드"));

      CommentCreateRequest request = new CommentCreateRequest(feed.getId(), testUser.getId(),
          " "); // 내용이 공백
      String requestJson = objectMapper.writeValueAsString(request);

      // when & then
      mockMvc.perform(post("/api/feeds/{feedId}/comments", feed.getId())
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestJson)
              .with(csrf()))
          .andDo(print())
          .andExpect(status().isBadRequest());
    }
  }
}

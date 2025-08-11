package com.stylemycloset.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stylemycloset.IntegrationTestSupport;
import com.stylemycloset.user.dto.request.ProfileUpdateRequest;
import com.stylemycloset.user.dto.request.UserCreateRequest;
import com.stylemycloset.user.entity.Gender;
import com.stylemycloset.user.entity.Role;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@AutoConfigureMockMvc
@Transactional
public class UserControllerTest extends IntegrationTestSupport {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  private UserCreateRequest userRequest1 = new UserCreateRequest("tester1", "tester1@naver.com",
      "testtest123!");

  private UserCreateRequest userRequest2 = new UserCreateRequest("tester2", "tester2@naver.com",
      "testtest123!");

  private ProfileUpdateRequest profileRequest = new ProfileUpdateRequest(
      "testName", Gender.MALE, LocalDate.of(2000, 1, 1), null, 3);

/*  @Test - 403오류 csrf구현을 안해서 현재는 불가능
  @DisplayName("유저 생성 API를 호출하면 유저가 생성되고 200 OK를 반환한다")
  void createUserApiTest() throws Exception {
    //given
    String jsonRequest = objectMapper.writeValueAsString(userRequest1);

    //when & then
    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRequest))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("tester1@naver.com"))
        .andExpect(jsonPath("$.name").value("tester1"))
        .andExpect(jsonPath("$.password").value("testtest123!"));
  }*/

  @Test
  @DisplayName("GET /api/users : 유저 목록 조회 API를 호출하면 유저 목록과 200 OK를 반환한다")
  @WithMockUser
  void getUsersApiTest() throws Exception {
    //given
    userRepository.saveAndFlush(
        new User(userRequest1.name(), userRequest1.email(), userRequest1.password()));
    userRepository.saveAndFlush(
        new User(userRequest2.name(), userRequest2.email(), userRequest2.password()));

    //when & then
    mockMvc.perform(get("/api/users")
            .param("limit", "5")
            .param("roleEqual", "USER"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.data[0].role").value(Role.USER.toString()));
  }

/*  @Test - 403오류 csrf구현을 안해서 현재는 불가능
  @DisplayName("유저 역할 변경 API를 호출하면 역할이 변경되고 200 OK를 반환한다")
  void changeRoleApiTest() throws Exception {
    // given
    User user = userRepository.save(
        new User(userRequest1));
    UserRoleUpdateRequest request = new UserRoleUpdateRequest(Role.ADMIN);
    String jsonRequest = objectMapper.writeValueAsString(request);

    // when & then
    mockMvc.perform(patch("/api/users/{id}/role", user.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRequest))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.role").value("ADMIN"));
  }*/

/*  @Test - 403오류 csrf구현을 안해서 현재는 불가능
  @DisplayName("비밀번호 변경 API를 호출하면 204 No Content를 반환한다")
  void changePasswordApiTest() throws Exception {
    //given
    User user = userRepository.save(
        new User(userRequest1));
    ChangePasswordRequest request = new ChangePasswordRequest("newPassword123!");
    String jsonRequest = objectMapper.writeValueAsString(request);

    //when & then
    mockMvc.perform(patch("/api/users/{id}/password", user.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRequest))
        .andDo(print())
        .andExpect(status().isNoContent());
  }*/

/*  @Test - 403오류 csrf구현을 안해서 현재는 불가능
  @DisplayName("유저 잠금 API를 호출하면 상태가 변경되고 200 OK를 반환한다")
  void lockUserApiTest() throws Exception {
    // given
    User user = userRepository.save(
        new User(userRequest1));
    UserLockUpdateRequest request = new UserLockUpdateRequest(true);
    String jsonRequest = objectMapper.writeValueAsString(request);

    // when & then
    mockMvc.perform(patch("/api/users/{id}/lock", user.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRequest))
        .andDo(print())
        .andExpect(status().isOk());

    User lockedUser = userRepository.findById(user.getId()).get();
    assertThat(lockedUser.isLocked()).isTrue();
  }*/

/*  @Test - 403오류 csrf구현을 안해서 현재는 불가능
  @DisplayName("유저 삭제 API를 호출하면 204 No Content를 반환한다")
  void softDeleteUserApiTest() throws Exception {
    // given
    User user = userRepository.save(
        new User(userRequest1));

    // when & then
    mockMvc.perform(delete("/api/users/{id}", user.getId()))
        .andDo(print())
        .andExpect(status().isNoContent());

    User deletedUser = userRepository.findById(user.getId()).get();
    assertThat(deletedUser.getDeleteAt()).isNotNull();
  }*/


/*  @Test - 403오류 csrf구현을 안해서 현재는 불가능
  @DisplayName("프로필 수정 API를 호출하면 프로필이 수정되고 200 OK를 반환한다")
  void updateProfileApiTest() throws Exception {
    // given
    User user = userRepository.saveAndFlush(
        new User(userRequest1));

    ProfileUpdateRequest request = new ProfileUpdateRequest("update", Gender.MALE,
        LocalDate.of(2000, 10, 10), null, 3);

    String jsonRequest = objectMapper.writeValueAsString(request);

    // when & then
    mockMvc.perform(patch("/api/users/{id}/profiles", user.getId(), null)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRequest))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("update"))
        .andExpect(jsonPath("$.gender").value(Gender.MALE.toString()))
        .andExpect(jsonPath("$.dateOfBirth").value(LocalDate.of(2000, 10, 10).toString()));
  }*/

  @Test
  @DisplayName("프로필 조회 API를 호출하면 프로필 정보와 200 OK를 반환한다")
  @WithMockUser
  void getProfileApiTest() throws Exception {
    // given
    User user = new User(userRequest1.name(), userRequest1.email(), userRequest1.password());
    user.updateProfile(profileRequest);
    User savedUser = userRepository.saveAndFlush(user);

    // when & then
    mockMvc.perform(get("/api/users/{userId}/profiles", savedUser.getId()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("testName"))
        .andExpect(jsonPath("$.gender").value(Gender.MALE.toString()))
        .andExpect(jsonPath("$.birthDate").value(LocalDate.of(2000, 1, 1).toString()));
  }
}

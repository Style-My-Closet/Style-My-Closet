package com.stylemycloset.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stylemycloset.security.jwt.JwtService;
import com.stylemycloset.user.dto.data.ProfileDto;
import com.stylemycloset.user.dto.data.UserDto;
import com.stylemycloset.user.dto.request.ChangePasswordRequest;
import com.stylemycloset.user.dto.request.ProfileUpdateRequest;
import com.stylemycloset.user.dto.request.ResetPasswordRequest;
import com.stylemycloset.user.dto.request.UserCreateRequest;
import com.stylemycloset.user.dto.request.UserLockUpdateRequest;
import com.stylemycloset.user.dto.request.UserPageRequest;
import com.stylemycloset.user.dto.request.UserRoleUpdateRequest;
import com.stylemycloset.user.dto.response.UserCursorResponse;
import com.stylemycloset.user.entity.Gender;
import com.stylemycloset.user.entity.Role;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.mapper.UserMapper;
import com.stylemycloset.user.repository.UserRepository;
import com.stylemycloset.user.util.MailService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mail.MailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @InjectMocks
  private UserServiceImpl userService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserMapper userMapper;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JwtService jwtService;

  @Mock
  private MailService mailService;

  private final UserCreateRequest testUserCreateRequest = new UserCreateRequest("tester",
      "test@naver.com", "testtest123!");

  @Test
  @DisplayName("유저 생성 테스트")
  public void CreateUserTest() throws Exception {
    //given
    User testUser = createTestUser(testUserCreateRequest);
    UserDto testUserDto = createTestUserDto(testUser);
    given(userRepository.existsByEmail(testUser.getEmail())).willReturn(false);
    given(userRepository.save(any(User.class))).willReturn(testUser);
    given(userMapper.UsertoUserDto(testUser)).willReturn(testUserDto);

    //when
    UserDto result = userService.createUser(testUserCreateRequest);

    //then
    assertNotNull(result);
    assertEquals(testUserDto.email(), result.email());
    assertEquals(testUserDto.name(), result.name());
  }

  @Test
  @DisplayName("유저 역할 업데이트 테스트")
  public void UpdateUserTest() throws Exception {
    //given
    UserRoleUpdateRequest request = new UserRoleUpdateRequest(Role.ADMIN);
    User testUser = createTestUser(testUserCreateRequest);
    UserDto testUserDto = createTestUserDto(testUser);
    final Long userId = 1L;

    given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
    given(userMapper.UsertoUserDto(testUser)).willReturn(testUserDto);
    //when
    UserDto result = userService.updateRole(userId, request);
    //then
    assertNotNull(result);
    assertEquals(Role.ADMIN, testUser.getRole());
    assertEquals(testUserDto, result);

    verify(userRepository).findById(userId);
    verify(eventPublisher, times(1)).publishEvent(any(Object.class));
  }

  @Test
  @DisplayName("유저 비밀번호 변경 테스트")
  public void changePasswordTest() throws Exception {
    //given
    final Long userId = 1L;
    User testUser = createTestUser(testUserCreateRequest);
    ChangePasswordRequest request = new ChangePasswordRequest("test123!");
    given(userRepository.findById(userId)).willReturn(Optional.of(testUser));

    String encodedPassword = passwordEncoder.encode("test123!");
    //when
    userService.changePassword(userId, request);
    //then
    assertEquals(testUser.getPassword(), encodedPassword);
  }

  @Test
  @DisplayName("유저 계정 잠금 테스트")
  public void lockUserTest() throws Exception {
    //given
    final Long userId = 1L;
    User testUser = createTestUser(testUserCreateRequest);
    boolean newLocked = true;
    UserLockUpdateRequest request = new UserLockUpdateRequest(newLocked);
    given(userRepository.findById(userId)).willReturn(Optional.of(testUser));

    //when
    userService.changeLockUser(userId, request);

    //then
    assertEquals(newLocked, testUser.isLocked());
  }

  @Test
  @DisplayName("유저 삭제 테스트")
  public void deleteUserTest() throws Exception {
    //given
    final Long userId = 1L;
    User testUser = createTestUser(testUserCreateRequest);
    given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
    assertNull(testUser.getDeletedAt());

    //when
    userService.deleteUser(userId);

    //then
    assertNotNull(testUser.getDeletedAt());
  }

  @Test
  @DisplayName("프로필 업데이트 테스트")
  public void updatedProfileTest() throws Exception {
    //given
    final Long userId = 1L;
    User testUser = createTestUser(testUserCreateRequest);
    ProfileDto testProfileDto = createTestProfileDto(testUser);
    ProfileUpdateRequest request = new ProfileUpdateRequest(
        "tester", Gender.MALE, LocalDate.of(2000, 10, 5), null, 3);
    given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
    given(userMapper.UsertoProfileDto(testUser)).willReturn(testProfileDto);

    assertNull(testUser.getGender());
    assertNull(testUser.getBirthDate());
    assertNull(testUser.getTemperatureSensitivity());

    //when
    userService.updateProfile(userId, request, null);

    //then
    assertEquals(request.gender(), testUser.getGender());
    assertEquals(request.birthDate(), testUser.getBirthDate());
    assertEquals(request.temperatureSensitivity(), testUser.getTemperatureSensitivity());
  }

  @Test
  @DisplayName("프로필 조회")
  public void getProfileTest() throws Exception {
    //given
    final Long userId = 1L;
    User testUser = createTestUser(testUserCreateRequest);
    ProfileDto testProfileDto = createTestProfileDto(testUser);
    given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
    given(userMapper.UsertoProfileDto(testUser)).willReturn(testProfileDto);

    //when
    ProfileDto result = userService.getProfile(userId);

    //then
    assertNotNull(result);
    assertEquals(testProfileDto, result);

  }

  @Nested
  @DisplayName("페이지 네이션 테스트")
  class cursorPageTest {

    @Test
    @DisplayName("hasNext가 true일때(다음 페이지 존재)")
    void hasNext_true() throws Exception {
      //given
      List<User> testUsers = new ArrayList<>();
      for (int i = 0; i < 6; i++) {
        User user = new User("tester" + i, "test" + i + "@naver.com",
            "test123!");
        user.setId((long) i + 1);
        testUsers.add(user);
      }

      UserPageRequest request = new UserPageRequest(null, null, 5, "name", "ASCENDING", null, null,
          null);

      given(userRepository.findUsersByCursor(request)).willReturn(testUsers);
      given(userRepository.countByFilter(request)).willReturn(100);
      given(userMapper.UsertoUserDto(any(User.class)))
          .willAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return new UserDto(user.getId(), user.getCreatedAt(), user.getEmail(), user.getName(),
                user.getRole(), Collections.emptyList(), false);
          });

      //when
      UserCursorResponse response = userService.getUser(request);

      //then
      assertTrue(response.hasNext());
      assertEquals(5, response.data().size());
      assertEquals(100, response.totalCount());

      assertEquals("tester4", response.nextCursor());
      assertEquals(5L, response.nextIdAfter());

      verify(userRepository).findUsersByCursor(request);
    }

    @Test
    @DisplayName("hasNext가 false일때(마지막 페이지)")
    void hasNext_false() throws Exception {
      //given
      List<User> testUsers = new ArrayList<>();
      for (int i = 0; i < 3; i++) {
        User user = new User("tester" + i, "test" + i + "@naver.com",
            "test123!");
        user.setId((long) i + 1);
        testUsers.add(user);
      }
      UserPageRequest request = new UserPageRequest("user2", 3L, 5, "name", "ASCENDING", null, null,
          null);

      given(userRepository.findUsersByCursor(request)).willReturn(testUsers);
      given(userMapper.UsertoUserDto(any(User.class)))
          .willAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return new UserDto(user.getId(), user.getCreatedAt(), user.getEmail(), user.getName(),
                user.getRole(), Collections.emptyList(), false);
          });

      //when
      UserCursorResponse response = userService.getUser(request);

      //then
      assertFalse(response.hasNext());
      assertEquals(3, response.data().size());

      assertNull(response.nextCursor());
      assertNull(response.nextIdAfter());


    }

  }

  @Test
  @DisplayName("비밀번호 초기화 테스트")
  void resetPassword_Success_WhenUserExists() {
    //given
    User testUser = createTestUser(testUserCreateRequest);

    String userEmail = "test@naver.com";
    ResetPasswordRequest request = new ResetPasswordRequest(userEmail);
    String encodedPassword = "encodedRandomPassword";

    when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
    when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);

    //when
    userService.resetPassword(request);

    //then
    verify(userRepository, times(1)).findByEmail(userEmail);

    assertNotNull(testUser.getTempPassword());
    assertNotNull(testUser.getResetPasswordTime());
  }

  //헬퍼 메소드
  private User createTestUser(UserCreateRequest request) {
    return new User(request.name(), request.email(), request.password());
  }

  private UserDto createTestUserDto(User user) {
    return new UserDto(
        user.getId(),
        user.getCreatedAt(),
        user.getEmail(),
        user.getName(),
        user.getRole(),
        List.of("google"),
        false
    );
  }

  private ProfileDto createTestProfileDto(User user) {
    return new ProfileDto(
        user.getId(),
        user.getName(),
        user.getGender(),
        user.getBirthDate(),
        user.getLocation(),
        user.getTemperatureSensitivity(),
        null
    );
  }


}

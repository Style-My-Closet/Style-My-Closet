package com.stylemycloset.security.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stylemycloset.security.ClosetUserDetails;
import com.stylemycloset.security.ClosetUserDetailsService;
import com.stylemycloset.user.dto.data.UserDto;
import com.stylemycloset.user.dto.request.UserCreateRequest;
import com.stylemycloset.user.entity.Role;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.mapper.UserMapper;
import com.stylemycloset.user.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserDetailServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserMapper userMapper;

  @InjectMocks
  private ClosetUserDetailsService closetUserDetailsService;

  @Test
  @DisplayName("사용자 조회 성공")
  void loadUserByUsername_Success() {
    //given
    UserCreateRequest request = new UserCreateRequest("tester", "test@naver.com", "testtest1!");

    User user = new User(request.name(), request.email(), request.password());

    UserDto userDto = new UserDto(1L, Instant.now(), request.email(), request.name(), Role.USER,
        null, false);

    // Mock 객체 동작 정의
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(userMapper.toUserDto(user)).thenReturn(userDto);

    //when
    ClosetUserDetails userDetails = (ClosetUserDetails) closetUserDetailsService.loadUserByUsername(
        user.getEmail());

    //then
    assertThat(userDetails).isNotNull();
    assertThat(userDetails.getEmail()).isEqualTo(user.getEmail());
    assertThat(userDetails.getUsername()).isEqualTo(user.getName());
    assertThat(userDetails.getPassword()).isEqualTo(user.getPassword());

    verify(userRepository, times(1)).findByEmail(user.getEmail());
    verify(userMapper, times(1)).toUserDto(user);
  }
}

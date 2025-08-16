package com.stylemycloset.auth.service;

import com.stylemycloset.user.dto.data.UserDto;
import com.stylemycloset.user.dto.request.UserCreateRequest;
import com.stylemycloset.user.entity.Role;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.mapper.UserMapper;
import com.stylemycloset.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

  @Value("${closet.admin.username}")
  private String username;
  @Value("${closet.admin.email}")
  private String email;
  @Value("${closet.admin.password}")
  private String password;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;

  @Override
  public UserDto adminInit() {
    if (userRepository.existsByEmail(email)) {
      log.warn("어드민이 이미 존재합니다.");
      return null;
    }
    UserCreateRequest request = new UserCreateRequest(username, email,
        passwordEncoder.encode(password));
    User admin = new User(request.name(), request.email(), request.password());
    admin.updateRole(Role.ADMIN);

    User saved = userRepository.save(admin);
    log.info("어드민 생성 완료");

    return userMapper.UsertoUserDto(saved);
  }
}

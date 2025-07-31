package com.stylemycloset.user.service;

import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.user.dto.data.UserDto;
import com.stylemycloset.user.dto.request.ChangePasswordRequest;
import com.stylemycloset.user.dto.request.UserCreateRequest;
import com.stylemycloset.user.dto.request.UserRoleUpdateRequest;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.exception.EmailDuplicateException;
import com.stylemycloset.user.exception.UserNotFoundException;
import com.stylemycloset.user.mapper.UserMapper;
import com.stylemycloset.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Transactional
  @Override
  public UserDto createUser(UserCreateRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new EmailDuplicateException();
    }

    User user = new User(request);
    User savedUser = userRepository.save(user);
    return userMapper.toDto(savedUser);
  }

  @Transactional
  @Override
  public UserDto updateRole(Long userId, UserRoleUpdateRequest updateRequest) {
    User user = userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);

    user.updateRole(updateRequest.role());
    return userMapper.toDto(user);
  }

  @Transactional
  @Override
  public void changePassword(Long userId, ChangePasswordRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);

    user.changePassword(request.password());
  }

  @Transactional
  @Override
  public void lockUser(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);

    user.lockUser();
  }

  @Transactional
  @Override
  public void deleteUser(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);

    user.softDelete();
  }
}

package com.stylemycloset.user.service;

import com.stylemycloset.user.dto.data.UserDto;
import com.stylemycloset.user.dto.request.ChangePasswordRequest;
import com.stylemycloset.user.dto.request.UserCreateRequest;
import com.stylemycloset.user.dto.request.UserRoleUpdateRequest;

public interface UserService {

  UserDto createUser(UserCreateRequest request);

  UserDto updateRole(Long userId, UserRoleUpdateRequest updateRequest);

  void changePassword(Long userId, ChangePasswordRequest request);

  void lockUser(Long userId);

  void deleteUser(Long userId);
}

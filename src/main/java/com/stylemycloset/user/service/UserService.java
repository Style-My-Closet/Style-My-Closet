package com.stylemycloset.user.service;

import com.stylemycloset.user.dto.data.ProfileDto;
import com.stylemycloset.user.dto.data.UserDto;
import com.stylemycloset.user.dto.request.ChangePasswordRequest;
import com.stylemycloset.user.dto.request.ProfileUpdateRequest;
import com.stylemycloset.user.dto.request.UserCreateRequest;
import com.stylemycloset.user.dto.request.UserPageRequest;
import com.stylemycloset.user.dto.request.UserRoleUpdateRequest;
import com.stylemycloset.user.dto.response.UserDtoCursorResonse;
import com.stylemycloset.user.entity.Role;
import java.awt.Cursor;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

  UserDto createUser(UserCreateRequest request);

  UserDto updateRole(Long userId, UserRoleUpdateRequest updateRequest);

  void changePassword(Long userId, ChangePasswordRequest request);

  void lockUser(Long userId);

  void deleteUser(Long userId);

  ProfileDto updateProfile(Long userId, ProfileUpdateRequest request, MultipartFile image);

  ProfileDto getProfile(Long userId);

  UserDtoCursorResonse getUser(UserPageRequest request);
}

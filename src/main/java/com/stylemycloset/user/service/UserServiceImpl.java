package com.stylemycloset.user.service;

import com.stylemycloset.notification.event.domain.RoleChangedEvent;
import com.stylemycloset.security.jwt.JwtService;
import com.stylemycloset.user.dto.data.ProfileDto;
import com.stylemycloset.user.dto.data.UserDto;
import com.stylemycloset.user.dto.request.ChangePasswordRequest;
import com.stylemycloset.user.dto.request.ProfileUpdateRequest;
import com.stylemycloset.user.dto.request.UserCreateRequest;
import com.stylemycloset.user.dto.request.UserLockUpdateRequest;
import com.stylemycloset.user.dto.request.UserPageRequest;
import com.stylemycloset.user.dto.request.UserRoleUpdateRequest;
import com.stylemycloset.user.dto.response.UserCursorResponse;
import com.stylemycloset.user.entity.Role;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.exception.EmailDuplicateException;
import com.stylemycloset.user.exception.UserNotFoundException;
import com.stylemycloset.user.mapper.UserMapper;
import com.stylemycloset.user.repository.UserRepository;
import com.stylemycloset.user.util.MailService;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final ApplicationEventPublisher publisher;
  private final UserMapper userMapper;
  private final JwtService jwtService;
  private final MailService mailSender;

  @Transactional
  @Override
  public UserDto createUser(UserCreateRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new EmailDuplicateException();
    }
    String encodedPassword = passwordEncoder.encode(request.password());

    User user = new User(request.name(), request.email(), encodedPassword);
    User savedUser = userRepository.save(user);
    return userMapper.UsertoUserDto(savedUser);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  @Override
  public UserDto updateRole(Long userId, UserRoleUpdateRequest updateRequest) {
    User user = findUserById(userId);

    Role previousRole = user.getRole();

    user.updateRole(updateRequest.role());
    publisher.publishEvent(new RoleChangedEvent(userId, previousRole));
    return userMapper.UsertoUserDto(user);
  }

  @Transactional
  @Override
  public void changePassword(Long userId, ChangePasswordRequest request) {
    User user = findUserById(userId);

    String encodedPassword = passwordEncoder.encode(request.password());

    user.changePassword(encodedPassword);
    if (user.getTempPassword() != null) {
      user.resetTempPassword(null, null);
    }
  }

  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  @Override
  public void changeLockUser(Long userId, UserLockUpdateRequest updateRequest) {
    User user = findUserById(userId);
    boolean locked = updateRequest.locked();
    user.lockUser(locked);

    if (locked) {
      jwtService.invalidateJwtSession(userId);
    }
  }


  @Transactional
  @Override
  public void deleteUser(Long userId) {
    User user = findUserById(userId);

    user.softDelete();
  }

  @Transactional
  @Override
  public ProfileDto updateProfile(Long userId, ProfileUpdateRequest request,
      MultipartFile image) {//이미지 구현은 나중에 binarycontent생기면 할예정
    User user = findUserById(userId);

    user.updateProfile(request);
    return userMapper.UsertoProfileDto(user);
  }

  @Transactional(readOnly = true)
  @Override
  public ProfileDto getProfile(Long userId) {
    User user = findUserById(userId);

    return userMapper.UsertoProfileDto(user);
  }

  @Override
  public UserCursorResponse getUser(UserPageRequest request) {
    List<User> users = userRepository.findUsersByCursor(request);

    boolean hasNext = users.size() > request.limit();

    List<User> content = hasNext ? users.subList(0, request.limit()) : users;

    List<UserDto> userDtos = content.stream().map(userMapper::UsertoUserDto).toList();

    String nextCursor = null;
    Long nextIdAfter = null;
    if (hasNext) {
      User lastUser = content.get(content.size() - 1);
      switch (request.sortBy()) {
        case "createdAt" -> {
          Instant value = lastUser.getCreatedAt();
          if (value != null) {
            nextCursor = value.toString();
          }
        }
        case "name" -> nextCursor = lastUser.getName();
        case "email" -> nextCursor = lastUser.getEmail();
        default -> throw new IllegalArgumentException("정렬 필드 오류");
      }
      nextIdAfter = lastUser.getId();
    }

    Integer totalCount = null;
    if (request.cursor() == null) {
      totalCount = (int) userRepository.countByFilter(request);
    }

    return new UserCursorResponse(
        userDtos,
        nextCursor,
        nextIdAfter,
        hasNext,
        totalCount,
        request.sortBy(),
        request.sortDirection()
    );
  }

  @Override
  @Transactional
  public void resetPassword(ResetPasswordRequest request) {
    User user = findUserByEmail(request.email());

    String randomPassword = RandomStringUtils.randomAlphanumeric(8);

    user.resetTempPassword(passwordEncoder.encode(randomPassword),
        Instant.now().plus(3, ChronoUnit.MINUTES));

    mailSender.sendTempPassword(user.getEmail(), randomPassword);
  }


  private User findUserById(Long userId) {
    return userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
  }

  private User findUserByEmail(String email) {
    return userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
  }
}

package com.stylemycloset.user.controller;

import com.stylemycloset.user.dto.data.ProfileDto;
import com.stylemycloset.user.dto.data.UserDto;
import com.stylemycloset.user.dto.request.ChangePasswordRequest;
import com.stylemycloset.user.dto.request.ProfileUpdateRequest;
import com.stylemycloset.user.dto.request.UserCreateRequest;
import com.stylemycloset.user.dto.request.UserLockUpdateRequest;
import com.stylemycloset.user.dto.request.UserPageRequest;
import com.stylemycloset.user.dto.request.UserRoleUpdateRequest;
import com.stylemycloset.user.dto.response.UserDtoCursorResonse;
import com.stylemycloset.user.entity.Role;
import com.stylemycloset.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  @PostMapping("")
  public ResponseEntity<UserDto> createUser(@RequestBody UserCreateRequest request) {
    return ResponseEntity.ok(userService.createUser(request));
  }

  @GetMapping("")
  public ResponseEntity<UserDtoCursorResonse> getUsers(
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) Long idAfter,
      @RequestParam(defaultValue = "10") int limit,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "DESCENDING") String sortDirection,
      @RequestParam(required = false) String emailLike,
      @RequestParam(required = false) Role roleEqual,
      @RequestParam(required = false) Boolean locked
  ) {
    UserPageRequest request = new UserPageRequest(
        cursor, idAfter, limit, sortBy, sortDirection,
        emailLike, roleEqual, locked
    );
    return ResponseEntity.ok(userService.getUser(request));
  }

  @PatchMapping("/{userId}/role")
  public ResponseEntity<UserDto> changeRole(@PathVariable Long userId,
      @RequestBody UserRoleUpdateRequest updateRequest) {
    return ResponseEntity.ok(userService.updateRole(userId, updateRequest));
  }

  @PatchMapping("/{userId}/password")
  public ResponseEntity<Void> changePassword(@PathVariable Long userId,
      @RequestBody ChangePasswordRequest changePasswordRequest) {
    userService.changePassword(userId, changePasswordRequest);

    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{userId}/lock")
  public ResponseEntity<Long> lockUser(@PathVariable Long userId,
      @RequestBody UserLockUpdateRequest userLockUpdateRequest) {
    userService.changeLockUser(userId, userLockUpdateRequest);

    return ResponseEntity.ok(userId);
  }

  @DeleteMapping("/{userId}") //없는 api긴 한데 일단 만들어 놨습니다.
  public ResponseEntity<Void> softDeleteUser(@PathVariable Long userId) {
    userService.deleteUser(userId);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{userId}/profiles")
  public ResponseEntity<ProfileDto> updateProfile(
      @PathVariable Long userId,
      @RequestPart("request") ProfileUpdateRequest request,
      @RequestPart(value = "image", required = false) MultipartFile image) {

    return ResponseEntity.ok(userService.updateProfile(userId, request, image));
  }

  @GetMapping("/{userId}/profiles")
  public ResponseEntity<ProfileDto> getProfile(@PathVariable Long userId) {
    return ResponseEntity.ok(userService.getProfile(userId));
  }


}

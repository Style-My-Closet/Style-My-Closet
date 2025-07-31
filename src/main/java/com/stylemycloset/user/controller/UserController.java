package com.stylemycloset.user.controller;

import com.stylemycloset.user.dto.data.UserDto;
import com.stylemycloset.user.dto.request.ChangePasswordRequest;
import com.stylemycloset.user.dto.request.UserCreateRequest;
import com.stylemycloset.user.dto.request.UserRoleUpdateRequest;
import com.stylemycloset.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  @PostMapping("")
  public ResponseEntity<UserDto> createUser(@RequestBody UserCreateRequest request) {
    return ResponseEntity.ok(userService.createUser(request));
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
  public ResponseEntity<Long> lockUser(@PathVariable Long userId) {
    userService.lockUser(userId);

    return ResponseEntity.ok(userId);
  }

  @DeleteMapping("/{userId}") //없는 api긴 한데 일단 만들어 놨습니다.
  public ResponseEntity<Void> softDeleteUser(@PathVariable Long userId) {
    userService.deleteUser(userId);
    return ResponseEntity.noContent().build();
  }


}

package com.stylemycloset.notification.controller;

import com.stylemycloset.notification.dto.NotificationDtoCursorResponse;
import com.stylemycloset.notification.dto.NotificationFindAllRequest;
import com.stylemycloset.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

  private final NotificationService notificationService;

  @DeleteMapping("/{notificationId}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> delete(
      @AuthenticationPrincipal(expression = "userId") Long userId,
      @PathVariable(name = "notificationId") long notificationId
  ) {
    notificationService.delete(userId, notificationId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping()
  public ResponseEntity<NotificationDtoCursorResponse> findAllByReceiverId(
      @AuthenticationPrincipal(expression = "userId") Long userId,
      @Valid NotificationFindAllRequest request
  ) {
    NotificationDtoCursorResponse res = notificationService.findAllByCursor(userId, request);
    return ResponseEntity.ok().body(res);
  }
}

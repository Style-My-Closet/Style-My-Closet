package com.stylemycloset.notification.controller;

import com.stylemycloset.notification.dto.NotificationDtoCursorResponse;
import com.stylemycloset.notification.dto.NotificationFindAllRequest;
import com.stylemycloset.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

  @DeleteMapping("/{receiverId}/{notificationId}")
  public ResponseEntity<Void> delete(
      // UserDetails의 userId로 대체 예정.
      @PathVariable long receiverId,
      @PathVariable long notificationId
  ) {
    notificationService.delete(receiverId, notificationId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{userId}")
  public ResponseEntity<NotificationDtoCursorResponse> findAllByReceiverId(
      // UserDetails의 userId로 대체 예정.
      @PathVariable long userId,
      NotificationFindAllRequest request
  ) {
    NotificationDtoCursorResponse res = notificationService.findAll(userId, request);
    return ResponseEntity.ok().body(res);
  }
}

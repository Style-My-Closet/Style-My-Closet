package com.stylemycloset.directmessage.controller;

import com.stylemycloset.directmessage.dto.DirectMessageResult;
import com.stylemycloset.directmessage.dto.request.DirectMessageCreateRequest;
import com.stylemycloset.directmessage.dto.request.DirectMessageSearchCondition;
import com.stylemycloset.directmessage.dto.response.DirectMessageResponse;
import com.stylemycloset.directmessage.entity.DirectMessageKey;
import com.stylemycloset.directmessage.service.DirectMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DirectMessageController {

  private final DirectMessageService directMessageService;
  private final SimpMessagingTemplate messagingTemplate;

  @MessageMapping("/direct-messages_send")
  public DirectMessageResult send(@Valid DirectMessageCreateRequest request) {
    DirectMessageResult message = directMessageService.create(request);

    String directMessageKey = DirectMessageKey.of(message);
    String destination = "/sub/direct-messages_" + directMessageKey;
    messagingTemplate.convertAndSend(destination, message);
    return message;
  }

  @GetMapping("/api/direct-messages")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<DirectMessageResponse<DirectMessageResult>> getDirectMessageByUser(
      @Valid DirectMessageSearchCondition condition,
      @AuthenticationPrincipal(expression = "userId") Long viewerId
  ) {
    DirectMessageResponse<DirectMessageResult> messages = directMessageService.getDirectMessageBetweenParticipants(
        condition, viewerId);
    return ResponseEntity.ok(messages);
  }

}

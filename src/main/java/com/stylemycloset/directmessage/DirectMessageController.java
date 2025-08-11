package com.stylemycloset.directmessage;

import com.stylemycloset.directmessage.dto.request.DirectMessageCreateRequest;
import com.stylemycloset.directmessage.dto.DirectMessageResult;
import com.stylemycloset.directmessage.entity.DirectMessageKey;
import com.stylemycloset.directmessage.service.DirectMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DirectMessageController {

  private final DirectMessageService directMessageService;
  private final SimpMessagingTemplate messagingTemplate;

  @MessageMapping("/direct-messages_send") // MDC 추적하려면 인터셉터 필요
  public DirectMessageResult send(@Valid DirectMessageCreateRequest request) {
    DirectMessageResult message = directMessageService.create(request);

    String directMessageKey = DirectMessageKey.of(message);
    String destination = "/sub/direct-messages_" + directMessageKey;
    messagingTemplate.convertAndSend(destination, message);
    // TODO: 8/11/25 이거 보내다가 예외터지면 메세지 저장 취소되야함, 이벤트 분리 필요
    return message;
  }

}

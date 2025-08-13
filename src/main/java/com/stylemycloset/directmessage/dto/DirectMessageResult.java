package com.stylemycloset.directmessage.dto;

import com.stylemycloset.directmessage.dto.response.DirectMessageUserInfo;
import com.stylemycloset.directmessage.entity.DirectMessage;
import java.time.Instant;

public record DirectMessageResult(
    Long id,
    Instant createdAt,
    DirectMessageUserInfo sender,
    DirectMessageUserInfo receiver,
    String content
) {

  public static DirectMessageResult from(
      DirectMessage directMessage,
      DirectMessageUserInfo senderInfo,
      DirectMessageUserInfo receiverInfo
  ) {
    return new DirectMessageResult(
        directMessage.getId(),
        directMessage.getCreatedAt(),
        senderInfo,
        receiverInfo,
        directMessage.getContent()
    );
  }

}

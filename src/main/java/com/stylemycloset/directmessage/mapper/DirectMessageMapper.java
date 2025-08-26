package com.stylemycloset.directmessage.mapper;

import com.stylemycloset.binarycontent.mapper.BinaryContentMapper;
import com.stylemycloset.common.repository.CursorStrategy;
import com.stylemycloset.common.repository.CustomSliceImpl;
import com.stylemycloset.common.repository.NextCursorInfo;
import com.stylemycloset.directmessage.dto.DirectMessageResult;
import com.stylemycloset.directmessage.dto.response.DirectMessageResponse;
import com.stylemycloset.directmessage.dto.response.DirectMessageUserInfo;
import com.stylemycloset.directmessage.entity.DirectMessage;
import com.stylemycloset.directmessage.repository.cursor.DirectMessageField;
import com.stylemycloset.user.entity.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DirectMessageMapper {

  private final BinaryContentMapper binaryContentMapper;

  public DirectMessageResult toResult(DirectMessage directMessage) {
    User sender = directMessage.getSender();
    User receiver = directMessage.getReceiver();

    return DirectMessageResult.from(
        directMessage,
        DirectMessageUserInfo.from(
            sender,
            binaryContentMapper.extractUrl(sender.getProfileImage())
        ),
        DirectMessageUserInfo.from(
            receiver,
            binaryContentMapper.extractUrl(receiver.getProfileImage())
        )
    );
  }

  public DirectMessageResponse<DirectMessageResult> toMessageResponse(
      Slice<DirectMessage> messages
  ) {
    List<DirectMessageResult> messageResults = getMessageResults(messages);
    Order order = CustomSliceImpl.getOrder(messages);

    return DirectMessageResponse.of(
        messageResults,
        NextCursorInfo.directMessageCursor(messages, order.getProperty()),
        messages.hasNext(),
        null,
        order.getProperty(),
        order.getDirection().toString()
    );
  }

  private List<DirectMessageResult> getMessageResults(Slice<DirectMessage> messages) {
    return messages.getContent()
        .stream()
        .map(this::toResult)
        .toList();
  }

}

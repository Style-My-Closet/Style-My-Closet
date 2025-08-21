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
    List<DirectMessageResult> messageResults = messages.getContent()
        .stream()
        .map(this::toResult)
        .toList();
    Order order = CustomSliceImpl.getOrder(messages);

    return DirectMessageResponse.of(
        messageResults,
        extractNextCursorInfo(messages, order.getProperty()),
        messages.hasNext(),
        null,
        order.getProperty(),
        order.getDirection().toString()
    );
  }

  private NextCursorInfo extractNextCursorInfo(Slice<DirectMessage> messages, String sortBy) {
    if (sortBy == null || sortBy.isBlank() ||
        !messages.hasNext() || messages.getContent().isEmpty()
    ) {
      return new NextCursorInfo(null, null);
    }

    DirectMessage lastMessage = messages.getContent().get(messages.getContent().size() - 1);

    CursorStrategy<?, DirectMessage> cursorStrategy = DirectMessageField.resolveStrategy(sortBy);
    String cursor = cursorStrategy.extract(lastMessage).toString();
    String idAfter = lastMessage.getId().toString();

    return new NextCursorInfo(cursor, idAfter);
  }

}

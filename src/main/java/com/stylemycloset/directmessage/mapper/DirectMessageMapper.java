package com.stylemycloset.directmessage.mapper;

import com.stylemycloset.binarycontent.storage.BinaryContentStorage;
import com.stylemycloset.directmessage.dto.DirectMessageResult;
import com.stylemycloset.directmessage.dto.response.DirectMessageResponse;
import com.stylemycloset.directmessage.dto.response.DirectMessageResponse.NextCursorInfo;
import com.stylemycloset.directmessage.dto.response.DirectMessageUserInfo;
import com.stylemycloset.directmessage.entity.DirectMessage;
import com.stylemycloset.common.repository.cursor.CursorStrategy;
import com.stylemycloset.directmessage.repository.cursor.DirectMessageField;
import com.stylemycloset.user.entity.User;
import java.net.URL;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DirectMessageMapper {

  private final BinaryContentStorage binaryContentStorage;

  public DirectMessageResult toResult(DirectMessage directMessage) {
    User sender = directMessage.getSender();
    User receiver = directMessage.getReceiver();

    return DirectMessageResult.from(
        directMessage,
        DirectMessageUserInfo.from(sender, getProfileImageURL(sender)),
        DirectMessageUserInfo.from(receiver, getProfileImageURL(receiver))
    );
  }

  private String getProfileImageURL(User user) {
    if (user.getProfileImage() == null) {
      return null;
    }
    URL url = binaryContentStorage.getUrl(user.getProfileImage().getId());
    return url.toString();
  }

  public DirectMessageResponse<DirectMessageResult> toMessageResponse(
      Slice<DirectMessage> messages
  ) {
    List<DirectMessageResult> messageResults = getMessageResults(messages);

    Order order = getOrder(messages);

    return DirectMessageResponse.of(
        messageResults,
        extractNextCursorInfo(messages, order.getProperty()),
        messages.hasNext(),
        null,
        order.getProperty(),
        order.getDirection().toString()
    );
  }


  private Order getOrder(Slice<DirectMessage> messages) {
    return messages.getPageable()
        .getSort()
        .stream()
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("DTO 변환시 정렬 순서(Order)가 존재하지 않습니다."));
  }

  private List<DirectMessageResult> getMessageResults(Slice<DirectMessage> messages) {
    return messages.getContent()
        .stream()
        .map(this::toResult)
        .toList();
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

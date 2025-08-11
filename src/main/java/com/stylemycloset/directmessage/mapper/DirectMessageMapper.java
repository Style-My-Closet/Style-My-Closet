package com.stylemycloset.directmessage.mapper;

import com.stylemycloset.binarycontent.storage.BinaryContentStorage;
import com.stylemycloset.directmessage.dto.DirectMessageResult;
import com.stylemycloset.directmessage.dto.response.DirectMessageUserInfo;
import com.stylemycloset.directmessage.entity.DirectMessage;
import com.stylemycloset.user.entity.User;
import java.net.URL;
import lombok.RequiredArgsConstructor;
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

}

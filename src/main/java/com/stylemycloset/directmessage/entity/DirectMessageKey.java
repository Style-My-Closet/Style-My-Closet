package com.stylemycloset.directmessage.entity;

import com.stylemycloset.directmessage.dto.DirectMessageResult;
import com.stylemycloset.user.entity.User;

public class DirectMessageKey {

  private DirectMessageKey() {
  }

  public static String of(DirectMessageResult directMessageResult) {
    Long senderId = directMessageResult.sender().id();
    Long receiverId = directMessageResult.receiver().id();
    if (senderId <= receiverId) {
      return senderId + "_" + receiverId;
    }
    return receiverId + "_" + senderId;
  }

  public static String of(User sender, User receiver) {// 이 부분도 수정해놓겠습니다.
    Long senderId = sender.getId();
    Long receiverId = receiver.getId();
    if (senderId <= receiverId) {
      return senderId + "_" + receiverId;
    }
    return receiverId + "_" + senderId;
  }

}

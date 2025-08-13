package com.stylemycloset.directmessage.entity.exception;

import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import java.util.Map;

public class MessageNotFoundException extends StyleMyClosetException {

  public MessageNotFoundException(Long messageId) {
    super(ErrorCode.MESSAGE_NOT_FOUND, Map.of("messageId", messageId));
  }
}

package com.stylemycloset.directmessage.exception;

import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import java.util.Map;

public final class DirectMessageNotFoundException extends StyleMyClosetException {

  public DirectMessageNotFoundException(Long messageId) {
    super(ErrorCode.MESSAGE_NOT_FOUND, Map.of("messageId", messageId));
  }

}

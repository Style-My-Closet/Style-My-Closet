package com.stylemycloset.directmessage.exception;

import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;

public abstract class DirectMessageException extends StyleMyClosetException {

  public DirectMessageException(ErrorCode errorCode) {
    super(errorCode);
  }

}

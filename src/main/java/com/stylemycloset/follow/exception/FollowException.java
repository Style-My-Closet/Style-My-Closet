package com.stylemycloset.follow.exception;

import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import java.util.Map;

public abstract class FollowException extends StyleMyClosetException {

  public FollowException(ErrorCode errorCode) {
    super(errorCode);
  }

}

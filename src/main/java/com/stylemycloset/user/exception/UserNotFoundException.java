package com.stylemycloset.user.exception;

import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import java.util.Map;

public class UserNotFoundException extends StyleMyClosetException {

  public UserNotFoundException() {
    super(ErrorCode.USER_NOT_FOUND, Map.of());
  }
}

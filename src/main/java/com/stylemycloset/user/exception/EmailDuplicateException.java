package com.stylemycloset.user.exception;

import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;

import java.util.Map;

public class EmailDuplicateException extends StyleMyClosetException {

  public EmailDuplicateException() {
    super(ErrorCode.EMAIL_DUPLICATED);
  }

}

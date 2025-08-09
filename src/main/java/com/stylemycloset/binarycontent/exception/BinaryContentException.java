package com.stylemycloset.binarycontent.exception;

import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import java.util.Map;

public class BinaryContentException extends StyleMyClosetException {

  public BinaryContentException(ErrorCode errorCode) {
    super(errorCode);
  }

}

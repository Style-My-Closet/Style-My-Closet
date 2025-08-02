package com.stylemycloset.binarycontent.exception;

import com.stylemycloset.common.exception.ErrorCode;
import java.util.Map;

public class BinaryContentNotFoundException extends BinaryContentException {

  public BinaryContentNotFoundException(Map<String, Object> details) {
    super(ErrorCode.ERROR_BINARY_CONTENT_NOT_FOUND, details);
  }

}

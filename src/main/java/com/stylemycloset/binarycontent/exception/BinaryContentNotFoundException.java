package com.stylemycloset.binarycontent.exception;

import com.stylemycloset.common.exception.ErrorCode;
import java.util.Map;

public final class BinaryContentNotFoundException extends BinaryContentException {

  public BinaryContentNotFoundException() {
    super(ErrorCode.ERROR_BINARY_CONTENT_NOT_FOUND);
  }

}

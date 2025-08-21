package com.stylemycloset.binarycontent.storage.s3.s3.exception;

import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import java.util.Map;

public abstract class CustomS3Exception extends StyleMyClosetException {

  public CustomS3Exception(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

}

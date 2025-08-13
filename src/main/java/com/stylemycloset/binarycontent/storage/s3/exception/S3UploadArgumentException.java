package com.stylemycloset.binarycontent.storage.s3.exception;

import com.stylemycloset.common.exception.ErrorCode;
import java.util.Map;

public class S3UploadArgumentException extends CustomS3Exception {

  public S3UploadArgumentException(Map<String, Object> details) {
    super(ErrorCode.ERROR_S3_UPLOAD_INVALID_ARGUMENT, details);
  }

}

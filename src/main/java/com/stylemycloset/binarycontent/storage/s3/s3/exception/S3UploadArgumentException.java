package com.stylemycloset.binarycontent.storage.s3.s3.exception;

import com.stylemycloset.common.exception.ErrorCode;

public class S3UploadArgumentException extends CustomS3Exception {

  public S3UploadArgumentException() {
    super(ErrorCode.ERROR_S3_UPLOAD_INVALID_ARGUMENT);
  }

}

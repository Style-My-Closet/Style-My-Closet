package com.stylemycloset.clothes.exception;

import com.stylemycloset.common.exception.ErrorCode;

public class ClothesExtractionFailedException extends ClothesException {

  public ClothesExtractionFailedException() {
    super(ErrorCode.CLOTHES_EXTRACTION_FAILED);
  }

}

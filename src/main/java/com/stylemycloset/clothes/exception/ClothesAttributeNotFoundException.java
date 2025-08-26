package com.stylemycloset.clothes.exception;

import com.stylemycloset.common.exception.ErrorCode;

public class ClothesAttributeNotFoundException extends ClothesException {

  public ClothesAttributeNotFoundException() {
    super(ErrorCode.ATTRIBUTE_NOT_FOUND);
  }

}

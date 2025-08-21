package com.stylemycloset.clothes.exception;

import com.stylemycloset.common.exception.ErrorCode;

public class ClothesAttributeDefinitionDuplicateException extends ClothesException {

  public ClothesAttributeDefinitionDuplicateException() {
    super(ErrorCode.ATTRIBUTE_DUPLICATE);
  }

}

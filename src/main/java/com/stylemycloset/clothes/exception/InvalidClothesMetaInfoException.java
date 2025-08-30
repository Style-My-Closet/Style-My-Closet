package com.stylemycloset.clothes.exception;

import com.stylemycloset.common.exception.ErrorCode;

public class InvalidClothesMetaInfoException extends ClothesException {

  public InvalidClothesMetaInfoException() {
    super(ErrorCode.INVALID_CLOTHES_META_INFO);
  }

}

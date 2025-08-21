package com.stylemycloset.clothes.exception;

import com.stylemycloset.common.exception.ErrorCode;

public final class ClothesNotFoundException extends ClothesException {

  public ClothesNotFoundException() {
    super(ErrorCode.CLOTHES_NOT_FOUND);
  }

}

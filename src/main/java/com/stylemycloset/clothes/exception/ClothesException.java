package com.stylemycloset.clothes.exception;

import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import lombok.Getter;

@Getter
public class ClothesException extends StyleMyClosetException {

  public ClothesException(ErrorCode errorCode) {
    super(errorCode);
  }

}

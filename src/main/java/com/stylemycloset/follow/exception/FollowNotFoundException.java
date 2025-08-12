package com.stylemycloset.follow.exception;

import com.stylemycloset.common.exception.ErrorCode;
import java.util.Map;

public final class FollowNotFoundException extends FollowException {

  public FollowNotFoundException() {
    super(ErrorCode.ERROR_FOLLOW_NOT_FOUND);
  }

}
